package dmcategories

enum class DmOrderMode(val value: Int) {
    STATIC(0),
    LAST_ACTIVITY(1);

    companion object {
        val DEFAULT = STATIC

        fun fromValue(value: Int): DmOrderMode = entries.firstOrNull { it.value == value } ?: DEFAULT
    }
}
