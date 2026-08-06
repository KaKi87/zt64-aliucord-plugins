package dmcategories

import android.content.Context
import android.view.animation.RotateAnimation
import com.aliucord.Utils
import com.aliucord.wrappers.ChannelWrapper.Companion.id
import com.discord.stores.StoreStream
import com.discord.views.CheckedSetting
import com.discord.widgets.channels.list.WidgetChannelsListAdapter
import com.discord.widgets.channels.list.items.ChannelListItemPrivate

private const val FLAG_FAVORITE = 1 shl 11
private const val DM_GUILD_ID = 0L

object Util {
    val expandAnimation: RotateAnimation = WidgetChannelsListAdapter.ItemChannelCategory.Companion.`access$getAnimation`(
        WidgetChannelsListAdapter.ItemChannelCategory.Companion,
        true
    )
    val collapseAnimation: RotateAnimation = WidgetChannelsListAdapter.ItemChannelCategory.Companion.`access$getAnimation`(
        WidgetChannelsListAdapter.ItemChannelCategory.Companion,
        false
    )

    fun getCurrentId() = StoreStream.getUsers().me.id

    fun getPinnedChannelIds(): List<Long> {
        return StoreStream.getUserGuildSettings()
            .guildSettings[DM_GUILD_ID]
            ?.channelOverrides
            ?.mapNotNull { override ->
                override.channelId.takeIf { override.flags and FLAG_FAVORITE != 0 }
            }
            ?: emptyList()
    }

    fun channelsInOrder(
        channelIds: List<Long>,
        channels: Map<Long, ChannelListItemPrivate>
    ): List<ChannelListItemPrivate> = channelIds.mapNotNull { channels[it] }

    fun moveChannelInCategory(category: DMCategory, channelId: Long, delta: Int): Boolean {
        val index = category.channelIds.indexOf(channelId)
        if (index == -1) return false

        val newIndex = index + delta
        if (newIndex !in category.channelIds.indices) return false

        category.channelIds.add(newIndex, category.channelIds.removeAt(index))
        return true
    }

    fun categoryChannels(
        mode: DmOrderMode,
        categoryChannelIds: List<Long>,
        privateChannels: List<ChannelListItemPrivate>,
        channelById: Map<Long, ChannelListItemPrivate>
    ): List<ChannelListItemPrivate> = when (mode) {
        DmOrderMode.STATIC -> channelsInOrder(categoryChannelIds, channelById)
        DmOrderMode.LAST_ACTIVITY -> privateChannels.filter { channel -> channel.channel.id in categoryChannelIds }
    }

    fun updateChannels() = StoreStream.`access$getDispatcher$p`(StoreStream.getPresences().stream).schedule {
        StoreStream.getMessagesMostRecent().markChanged()
    }

    fun createSwitch(context: Context, text: String, subText: String): CheckedSetting {
        return Utils.createCheckedSetting(context, CheckedSetting.ViewType.SWITCH, text, subText)
    }
}