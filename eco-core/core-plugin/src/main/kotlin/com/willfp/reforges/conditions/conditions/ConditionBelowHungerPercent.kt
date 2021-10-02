package com.willfp.reforges.conditions.conditions

import com.willfp.eco.core.config.interfaces.JSONConfig
import com.willfp.reforges.conditions.Condition
import com.willfp.reforges.conditions.updateReforge
import com.willfp.reforges.reforges.ReforgeLookup
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.FoodLevelChangeEvent


class ConditionBelowHungerPercent : Condition("below_hunger_percent") {
    @EventHandler(
        priority = EventPriority.MONITOR,
        ignoreCancelled = true
    )
    fun handle(event: FoodLevelChangeEvent) {
        val player = event.entity

        if (player !is Player) {
            return
        }

        val items = ReforgeLookup.provide(player)

        for (item in items) {
            item.updateReforge(player, this)
        }
    }

    override fun isConditionMet(player: Player, config: JSONConfig): Boolean {
        return (player.foodLevel / 20) * 100 <= config.getDouble("percent")
    }
}