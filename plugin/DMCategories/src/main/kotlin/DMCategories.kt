import android.content.Context
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import com.aliucord.Utils
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.api.SettingsAPI
import com.aliucord.entities.Plugin
import com.aliucord.patcher.*
import com.aliucord.settings.delegate
import com.aliucord.wrappers.ChannelWrapper.Companion.id
import com.aliucord.wrappers.ChannelWrapper.Companion.isDM
import com.discord.databinding.WidgetChannelsListItemActionsBinding
import com.discord.utilities.color.ColorCompat
import com.discord.widgets.channels.list.*
import com.discord.widgets.channels.list.items.ChannelListItemPrivate
import com.google.gson.reflect.TypeToken
import com.lytefast.flexinput.R
import dmcategories.DMCategory
import dmcategories.DmOrderMode
import dmcategories.PluginSettings
import dmcategories.Util
import dmcategories.items.*
import dmcategories.sheets.CategoriesSheet

private val categoryType = TypeToken
    .getParameterized(
        ArrayList::class.java,
        DMCategory::class.javaObjectType
    ).getType()

@AliucordPlugin
class DMCategories : Plugin() {
    private val getBindingMethod = WidgetChannelsListItemChannelActions::class.java
        .getDeclaredMethod("getBinding")
        .apply { isAccessible = true }

    private fun WidgetChannelsListItemChannelActions.getBinding() = getBindingMethod(this) as WidgetChannelsListItemActionsBinding

    private val SettingsAPI.showSelected: Boolean by settings.delegate(true)
    private val SettingsAPI.showUnread: Boolean by settings.delegate(false)
    private val SettingsAPI.hideEmpty: Boolean by settings.delegate(false)
    private var SettingsAPI.dmOrderMode: Int by settings.delegate(DmOrderMode.DEFAULT.value)

    init {
        settingsTab = SettingsTab(PluginSettings::class.java, SettingsTab.Type.BOTTOM_SHEET).withArgs(settings)
    }

    companion object {
        private lateinit var mSettings: SettingsAPI
        val categories: MutableList<DMCategory> by lazy {
            mSettings.getObject("categories", mutableListOf(), categoryType)
        }

        fun saveCategories() = mSettings.setObject("categories", categories)

        fun addCategory(name: String, channelIds: ArrayList<Long> = ArrayList()) {
            categories
                .add(DMCategory(Util.getCurrentId(), name, channelIds))
                .also { if (it) saveCategories() }
        }

        fun removeCategory(category: DMCategory) = categories.remove(category).also {
            if (it) saveCategories()
        }

        fun getCategory(name: String) = categories.find { dmCategory -> dmCategory.name == name }
    }

    override fun start(context: Context) {
        val categoryLayoutId = Utils.getResId("widget_channels_list_item_category", "layout")
        val stageEventsSeparatorId = Utils.getResId("widget_channels_list_item_stage_events_separator", "layout")

        mSettings = settings

        patcher.after<WidgetChannelsListItemChannelActions>(
            "configureUI",
            WidgetChannelsListItemChannelActions.Model::class.java
        ) { (_, model: WidgetChannelsListItemChannelActions.Model) ->
            if (!model.channel.isDM()) return@after

            val root = getBinding().root as NestedScrollView
            val ctx = root.context
            val menuLayout = root.getChildAt(0) as LinearLayout

            fun addMenuAction(text: String, drawable: Int, onClick: () -> Unit) {
                menuLayout.addView(
                    TextView(ctx, null, 0, R.i.UiKit_Settings_Item_Icon).apply {
                        this.text = text
                        setOnClickListener {
                            dismiss()
                            onClick()
                        }
                        setCompoundDrawablesWithIntrinsicBounds(
                            ContextCompat
                                .getDrawable(ctx, drawable)!!
                                .mutate()
                                .apply {
                                    setTint(ColorCompat.getThemedColor(ctx, R.b.colorInteractiveNormal))
                                },
                            null,
                            null,
                            null
                        )
                    }
                )
            }

            categories
                .find { category -> category.channelIds.contains(model.channel.id) }
                ?.let { category ->
                    val channelIndex = category.channelIds.indexOf(model.channel.id)

                    if (channelIndex > 0) {
                        addMenuAction("Move up", R.e.ic_arrow_up_24dp) {
                            if (Util.moveChannelInCategory(category, model.channel.id, -1)) {
                                saveCategories()
                                Util.updateChannels()
                            }
                        }
                    }

                    if (channelIndex < category.channelIds.lastIndex) {
                        addMenuAction("Move down", R.e.ic_arrow_down_24dp) {
                            if (Util.moveChannelInCategory(category, model.channel.id, 1)) {
                                saveCategories()
                                Util.updateChannels()
                            }
                        }
                    }

                    addMenuAction("Remove from category", R.e.ic_remove_circle_outline_red_24dp) {
                        category.channelIds.remove(model.channel.id)
                        saveCategories()
                        Util.updateChannels()
                    }
                } ?: addMenuAction("Set Category", R.e.ic_group_add_white_24dp) {
                    CategoriesSheet(model.channel.id).show(parentFragmentManager, "Categories")
                }
        }

        @OptIn(ExperimentalStdlibApi::class)
        patcher.before<WidgetChannelsList>("configureUI", WidgetChannelListModel::class.java) { (_, model: WidgetChannelListModel) ->
            // Only run if this is the DMs tab
            if (model.selectedGuild != null) return@before

            val currentUserId = Util.getCurrentId()
            val userCategories = categories.filter { category -> category.userId == currentUserId }
            if (userCategories.isEmpty()) return@before

            val privateChannels = model.items.filterIsInstance<ChannelListItemPrivate>()
            val channelById = privateChannels.associateBy { channel -> channel.channel.id }
            val orderMode = DmOrderMode.fromValue(settings.dmOrderMode)
            val categorizedChannelIds = mutableSetOf<Long>()

            val categoryItems = buildList(100) {
                userCategories.forEach { category ->
                    val channels = Util.categoryChannels(
                        orderMode,
                        category.channelIds,
                        privateChannels,
                        channelById
                    )
                    categorizedChannelIds.addAll(channels.map { channel -> channel.channel.id })

                    if (settings.hideEmpty && channels.isEmpty()) return@forEach

                    add(ChannelListItemDMCategory(category))

                    addAll(
                        elements = if (category.collapsed) {
                            if (!settings.showSelected && !settings.showUnread) return@forEach

                            channels.filter { channel ->
                                settings.showSelected &&
                                    channel.selected ||
                                    settings.showUnread &&
                                    !channel.muted &&
                                    channel.mentionCount > 0
                            }
                        } else {
                            channels
                        }
                    )
                }
            } + ChannelListItemDivider

            val uncategorizedChannels = when (orderMode) {
                DmOrderMode.STATIC -> {
                    val pinnedChannelIds = Util.getPinnedChannelIds()
                    val pinnedChannelIdSet = pinnedChannelIds.toSet()
                    val uncategorizedPinned = Util.channelsInOrder(
                        pinnedChannelIds.filter { channelId -> channelId !in categorizedChannelIds },
                        channelById
                    )
                    val uncategorizedUnpinned = privateChannels.filter { channel ->
                        channel.channel.id !in categorizedChannelIds && channel.channel.id !in pinnedChannelIdSet
                    }
                    uncategorizedPinned to uncategorizedUnpinned
                }

                DmOrderMode.LAST_ACTIVITY -> {
                    val uncategorized = privateChannels.filter { channel ->
                        channel.channel.id !in categorizedChannelIds
                    }
                    emptyList<ChannelListItemPrivate>() to uncategorized
                }
            }
            val otherItems = model.items.filter { item -> item !is ChannelListItemPrivate }

            model.items.clear()
            model.items.addAll(uncategorizedChannels.first)
            model.items.addAll(categoryItems)
            model.items.addAll(uncategorizedChannels.second)
            model.items.addAll(otherItems)
        }

        patcher.after<WidgetChannelsListAdapter>(
            "onCreateViewHolder",
            ViewGroup::class.java,
            Int::class.java
        ) { (param, _: Any, type: Int) ->
            param.result = when (type) {
                ChannelListItemDMCategory.TYPE -> ItemDMCategory(categoryLayoutId, this)
                ChannelListItemDivider.TYPE -> ItemDivider(stageEventsSeparatorId, this)
                else -> param.result
            }
        }
    }

    override fun stop(context: Context) = patcher.unpatchAll()
}