package dmcategories

import android.os.Bundle
import android.view.View
import android.widget.RadioGroup
import android.widget.TextView
import com.aliucord.Utils
import com.aliucord.api.SettingsAPI
import com.aliucord.settings.delegate
import com.aliucord.views.Divider
import com.aliucord.widgets.BottomSheet
import com.discord.views.CheckedSetting
import com.discord.views.RadioManager
import com.lytefast.flexinput.R

class PluginSettings(private val settings: SettingsAPI) : BottomSheet() {
    private var SettingsAPI.showSelected: Boolean by settings.delegate(true)
    private var SettingsAPI.showUnread: Boolean by settings.delegate(false)
    private var SettingsAPI.hideEmpty: Boolean by settings.delegate(false)
    private var SettingsAPI.dmOrderMode: Int by settings.delegate(DmOrderMode.DEFAULT.value)

    override fun onViewCreated(view: View, bundle: Bundle?) {
        super.onViewCreated(view, bundle)

        val ctx = requireContext()

        addView(
            TextView(ctx, null, 0, R.i.UiKit_Settings_Item_Header).apply {
                text = "DM Categories"
            }
        )
        addView(
            Util
                .createSwitch(
                    ctx,
                    "Show Selected",
                    "Whether selected channels should be visible even if the category is collapsed"
                ).apply {
                    isChecked = settings.showSelected
                    setOnCheckedListener { settings.showSelected = it }
                }
        )
        addView(
            Util
                .createSwitch(
                    ctx,
                    "Show Unread",
                    "Whether unread channels should be visible even if the category is collapsed"
                ).apply {
                    isChecked = settings.showUnread
                    setOnCheckedListener { settings.showUnread = it }
                }
        )
        addView(
            Util.createSwitch(
                ctx,
                "Hide if empty",
                "Whether categories should be hidden if they have no channels"
            ).apply {
                isChecked = settings.hideEmpty
                setOnCheckedListener { settings.hideEmpty = it }
            }
        )

        addView(Divider(ctx))
        addView(
            TextView(ctx, null, 0, R.i.UiKit_Settings_Item_Header).apply {
                text = "DM ordering"
            }
        )

        val orderOptions = listOf(
            OrderOption(
                DmOrderMode.STATIC,
                "Static order",
                "Keep pinned DMs in their user-defined order"
            ),
            OrderOption(
                DmOrderMode.LAST_ACTIVITY,
                "Last activity",
                "Sort DMs by most recent message"
            ),
        )

        val radios = orderOptions.map { option ->
            Utils.createCheckedSetting(ctx, CheckedSetting.ViewType.RADIO, option.title, option.description)
        }
        val radioManager = RadioManager(radios)
        val selectedMode = DmOrderMode.fromValue(settings.dmOrderMode)

        addView(
            RadioGroup(ctx).apply {
                orderOptions.forEachIndexed { index, option ->
                    val radio = radios[index]
                    radio.setOnClickListener {
                        settings.dmOrderMode = option.mode.value
                        radioManager.a(radio)
                        Util.updateChannels()
                    }
                    addView(radio)
                    if (option.mode == selectedMode) radioManager.a(radio)
                }
            }
        )
    }

    private data class OrderOption(
        val mode: DmOrderMode,
        val title: String,
        val description: String
    )
}