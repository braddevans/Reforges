package com.willfp.reforges.display

import com.willfp.eco.core.Prerequisite
import com.willfp.eco.core.display.Display
import com.willfp.eco.core.display.DisplayModule
import com.willfp.eco.core.display.DisplayPriority
import com.willfp.eco.core.fast.FastItemStack
import com.willfp.eco.util.SkullUtils
import com.willfp.eco.util.StringUtils
import com.willfp.reforges.ReforgesPlugin
import com.willfp.reforges.reforges.meta.ReforgeTarget
import com.willfp.reforges.reforges.util.ReforgeUtils
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextReplacementConfig
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.persistence.PersistentDataType

@Suppress("DEPRECATION")
class ReforgesDisplay(private val plugin: ReforgesPlugin) : DisplayModule(plugin, DisplayPriority.HIGHEST) {
    /**
     * Deprecated
     */
    @Deprecated("Use PDC components!")
    private val replacement = TextReplacementConfig.builder()
        .match("§w(.+)§w")
        .replacement("")
        .build()

    private val originalComponentKey = plugin.namespacedKeyFactory.create("real_name")
    private val serializer = GsonComponentSerializer.gson()

    override fun display(
        itemStack: ItemStack,
        vararg args: Any
    ) {
        val target = ReforgeTarget.getForItem(itemStack)

        val meta = itemStack.itemMeta ?: return

        val stone = ReforgeUtils.getReforgeStone(meta)

        if (target.isEmpty() && stone == null) {
            return
        }

        val fastItemStack = FastItemStack.wrap(itemStack)

        val lore = fastItemStack.lore

        val reforge = ReforgeUtils.getReforge(meta)

        if (reforge == null && stone == null && target != null) {
            if (plugin.configYml.getBool("reforge.show-reforgable")) {
                val addLore: MutableList<String> = ArrayList()
                for (string in plugin.configYml.getStrings("reforge.reforgable-suffix")) {
                    addLore.add(Display.PREFIX + string)
                }
                lore.addAll(addLore)
            }
        }

        if (stone != null) {
            meta.setDisplayName(stone.config.getFormattedString("stone.name"))
            val stoneMeta = stone.stone.itemMeta
            if (stoneMeta is SkullMeta) {
                val stoneTexture = SkullUtils.getSkullTexture(stoneMeta)

                if (stoneTexture != null) {
                    try {
                        SkullUtils.setSkullTexture(meta as SkullMeta, stoneTexture)
                    } catch (e: StringIndexOutOfBoundsException) {
                        // Do nothing
                    }
                }
            }
            itemStack.itemMeta = meta
            val stoneLore = stone.config.getFormattedStrings("stone.lore").map {
                "${Display.PREFIX}$it"
            }.toList()
            lore.addAll(0, stoneLore)
        }
        if (reforge != null) {
            if (plugin.configYml.getBool("reforge.display-in-lore")) {
                val addLore: MutableList<String> = ArrayList()
                addLore.add(" ")
                addLore.add(reforge.name)
                addLore.addAll(reforge.description)
                addLore.replaceAll { "${Display.PREFIX}$it" }
                lore.addAll(addLore)
            }
            if (plugin.configYml.getBool("reforge.display-in-name") && Prerequisite.HAS_PAPER.isMet) {
                val displayName = (meta.displayName() ?: Component.translatable(itemStack)).replaceText(replacement)
                meta.persistentDataContainer.set(
                    originalComponentKey,
                    PersistentDataType.STRING,
                    serializer.serialize(displayName)
                )
                val newName = StringUtils.toComponent("${reforge.name} ")
                    .decoration(TextDecoration.ITALIC, false).append(displayName)
                meta.displayName(newName)
            }
        }
        itemStack.itemMeta = meta
        fastItemStack.lore = lore
    }

    override fun revert(itemStack: ItemStack) {
        ReforgeTarget.getForItem(itemStack) ?: return

        val meta = itemStack.itemMeta ?: return

        if (plugin.configYml.getBool("reforge.display-in-name") && Prerequisite.HAS_PAPER.isMet) {
            val originalName =
                meta.persistentDataContainer.get(originalComponentKey, PersistentDataType.STRING) ?: return
            meta.persistentDataContainer.remove(originalComponentKey)
            meta.displayName(serializer.deserialize(originalName).replaceText(replacement))
        }

        itemStack.itemMeta = meta
    }
}