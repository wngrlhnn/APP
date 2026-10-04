package com.wngrlhnn.jeepexplorer

data class Jeep(val id: Int, val name: String, val subtitle: String, val category: Category, val color: Long, val accent: Long)

enum class Category(val title: String) {
    ALL("הכול"), OFF_ROAD("שטח"), PICKUP("פיקאפ"), FAMILY("משפחתיים"), ELECTRIC("חשמליים")
}

val jeeps = listOf(
    Jeep(1, "Wrangler Rubicon", "אגדת השטח", Category.OFF_ROAD, 0xFF22272B, 0xFFD9F36A),
    Jeep(2, "Wrangler Willys 392", "אגרסיבי ומיוחד", Category.OFF_ROAD, 0xFF48533F, 0xFFFFD166),
    Jeep(3, "Wrangler Sahara", "לחול ולשבילים", Category.OFF_ROAD, 0xFF80725D, 0xFFE5D8B3),
    Jeep(4, "Wrangler 4xe", "שטח עם הנעה חשמלית", Category.ELECTRIC, 0xFF244A45, 0xFF6DE3D2),
    Jeep(5, "Wrangler Mojito", "ירוק עם אופי", Category.OFF_ROAD, 0xFF315043, 0xFFA7F070),
    Jeep(6, "Gladiator Rubicon", "פיקאפ לשטח", Category.PICKUP, 0xFF2D4B62, 0xFF96C3E6),
    Jeep(7, "Gladiator Mojave", "Desert Rated", Category.PICKUP, 0xFFC46A2B, 0xFFFFD38A),
    Jeep(8, "Gladiator Night", "פיקאפ קשוח", Category.PICKUP, 0xFF25282C, 0xFFB6C5D1),
    Jeep(9, "Gladiator Freedom", "הרפתקה פתוחה", Category.PICKUP, 0xFF59636A, 0xFFD9F36A),
    Jeep(10, "Grand Cherokee", "SUV למשפחה", Category.FAMILY, 0xFF363B42, 0xFFB5C7D3),
    Jeep(11, "Grand Cherokee 4xe", "SUV היברידי", Category.ELECTRIC, 0xFF263A42, 0xFF78E8D4),
    Jeep(12, "Grand Cherokee Trailhawk", "שטח בלי פשרות", Category.OFF_ROAD, 0xFF403D35, 0xFFE6B85C),
    Jeep(13, "Grand Wagoneer", "יוקרה גדולה", Category.FAMILY, 0xFF6C4D3A, 0xFFE4C5A8),
    Jeep(14, "Wagoneer", "מרווח ומפנק", Category.FAMILY, 0xFF4A4C4B, 0xFFD0D5D2),
    Jeep(15, "Wagoneer S", "חשמלי ואלגנטי", Category.ELECTRIC, 0xFF25262B, 0xFF6DE3D2),
    Jeep(16, "Recon", "חשמלי להרפתקה", Category.ELECTRIC, 0xFF4A4A46, 0xFFB5F36D),
    Jeep(17, "Cherokee", "Turbo Hybrid", Category.FAMILY, 0xFF394B54, 0xFF9ED7E9),
    Jeep(18, "Compass", "קומפקטי והרפתקני", Category.FAMILY, 0xFF3D4248, 0xFFF0B35A),
    Jeep(19, "Compass Trailhawk", "קטן, קשוח, מוכן לשטח", Category.OFF_ROAD, 0xFF514A3D, 0xFFFFC857),
    Jeep(20, "Renegade", "אורבני עם נשמה", Category.FAMILY, 0xFF3B4248, 0xFFB8E986),
    Jeep(21, "Renegade Trailhawk", "קומפקטי לשטח", Category.OFF_ROAD, 0xFF604D3D, 0xFFE5B85C),
    Jeep(22, "Avenger", "חשמלי קטן וזריז", Category.ELECTRIC, 0xFF3A424A, 0xFF83E8FF),
    Jeep(23, "Patriot", "קלאסי ואמין", Category.FAMILY, 0xFF555B5D, 0xFFC9D1D4),
    Jeep(24, "Liberty", "שטח קלאסי", Category.OFF_ROAD, 0xFF4A4038, 0xFFE0A85B),
    Jeep(25, "Commander", "7 מושבים להרפתקה", Category.FAMILY, 0xFF34393D, 0xFFBFD1DA),
    Jeep(26, "CJ-7 Classic", "אייקון מהעבר", Category.OFF_ROAD, 0xFF5D5A4C, 0xFFE7D27A),
    Jeep(27, "CJ-8 Scrambler", "קלאסי עם ארגז", Category.PICKUP, 0xFF6A4735, 0xFFFFB86B),
    Jeep(28, "Willys MB", "המקורי מהשטח", Category.OFF_ROAD, 0xFF3F4A37, 0xFFB8D27A),
    Jeep(29, "Wrangler 2-Door", "קצר, חד, קשוח", Category.OFF_ROAD, 0xFF293038, 0xFF8FD3FF),
    Jeep(30, "Wrangler Xtreme", "גרסת אקסטרים", Category.OFF_ROAD, 0xFF202326, 0xFFFF6B6B)
)
