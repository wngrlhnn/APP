package com.wngrlhnn.jeepexplorer

data class Jeep(
    val id: Int,
    val name: String,
    val subtitle: String,
    val category: Category,
    val color: Long,
    val accent: Long
)

enum class Category(val title: String) {
    ALL("הכול"),
    OFF_ROAD("שטח"),
    PICKUP("פיקאפ"),
    FAMILY("משפחתיים"),
    ELECTRIC("חשמליים")
}

val jeeps = listOf(
    Jeep(1, "Wrangler Rubicon", "אגדת השטח", Category.OFF_ROAD, 0xFF22272B, 0xFFB8E986),
    Jeep(2, "Wrangler Willys 392", "אגרסיבי ומיוחד", Category.OFF_ROAD, 0xFF48533F, 0xFFD9C66B),
    Jeep(3, "Wrangler Rewind", "רטרו עם אופי", Category.OFF_ROAD, 0xFFB5B0A7, 0xFF2B2D30),
    Jeep(4, "Gladiator Rubicon", "פיקאפ לשטח", Category.PICKUP, 0xFF2D4B62, 0xFF96C3E6),
    Jeep(5, "Gladiator Mojave", "Desert Rated", Category.PICKUP, 0xFFC46A2B, 0xFFFFD38A),
    Jeep(6, "Grand Cherokee", "SUV למשפחה", Category.FAMILY, 0xFF363B42, 0xFFB5C7D3),
    Jeep(7, "Grand Wagoneer", "יוקרה גדולה", Category.FAMILY, 0xFF6C4D3A, 0xFFE4C5A8),
    Jeep(8, "Wagoneer S", "חשמלי ואלגנטי", Category.ELECTRIC, 0xFF25262B, 0xFF6DE3D2),
    Jeep(9, "Recon", "חשמלי להרפתקה", Category.ELECTRIC, 0xFF4A4A46, 0xFFB5F36D),
    Jeep(10, "Cherokee", "Turbo Hybrid", Category.FAMILY, 0xFF394B54, 0xFF9ED7E9),
    Jeep(11, "Compass", "קומפקטי והרפתקני", Category.FAMILY, 0xFF3D4248, 0xFFF0B35A),
    Jeep(12, "Wrangler Sahara", "לחול ולשבילים", Category.OFF_ROAD, 0xFF80725D, 0xFFE5D8B3)
)
