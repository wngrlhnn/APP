package com.wngrlhnn.jeepexplorer

data class Jeep(
    val id: Int,
    val name: String,
    val subtitle: String,
    val category: Category,
    val color: Long,
    val accent: Long,
    val photoRes: Int
)

enum class Category(val title: String) {
    ALL("הכול"), OFF_ROAD("שטח"), PICKUP("פיקאפ"), FAMILY("משפחתיים"), ELECTRIC("חשמליים")
}

val jeeps = listOf(
    Jeep(1, "Wrangler Rubicon", "אגדת השטח", Category.OFF_ROAD, 0xFF22272B, 0xFFD9F36A, R.drawable.jeep_tj),
    Jeep(2, "Wrangler TJ", "1997–2006 • קלאסי", Category.OFF_ROAD, 0xFF7A1F1F, 0xFFFFD166, R.drawable.jeep_tj),
    Jeep(3, "Wrangler YJ", "הפנסים המרובעים", Category.OFF_ROAD, 0xFF284A35, 0xFFA9E66E, R.drawable.jeep_yj),
    Jeep(4, "Wrangler Sahara", "2007 • 2 דלתות", Category.OFF_ROAD, 0xFF1E2837, 0xFF80C7FF, R.drawable.jeep_sahara),
    Jeep(5, "Wrangler Sahara Open", "גג פתוח • קיץ בשטח", Category.OFF_ROAD, 0xFF55613D, 0xFFE8D29C, R.drawable.jeep_sahara_open),
    Jeep(6, "Wrangler Sport", "קצר וקליל", Category.OFF_ROAD, 0xFF315B67, 0xFF8FE9EE, R.drawable.jeep_yj),
    Jeep(7, "Wrangler Unlimited", "4 דלתות • יותר מקום", Category.OFF_ROAD, 0xFF3D4148, 0xFFD9F36A, R.drawable.jeep_sahara),
    Jeep(8, "Wrangler 4xe", "היברידי עם אופי", Category.ELECTRIC, 0xFF244A45, 0xFF6DE3D2, R.drawable.jeep_sahara_open),

    Jeep(9, "Gladiator Willys", "פיקאפ לשטח", Category.PICKUP, 0xFF4C5E63, 0xFFDDEB9D, R.drawable.jeep_gladiator_willys),
    Jeep(10, "Gladiator Sahara", "פיקאפ להרפתקה", Category.PICKUP, 0xFF43586B, 0xFFB7E3FF, R.drawable.jeep_gladiator_sahara),
    Jeep(11, "Gladiator Rubicon", "פיקאפ קשוח", Category.PICKUP, 0xFF5A3827, 0xFFFFB86B, R.drawable.jeep_gladiator_willys),
    Jeep(12, "Gladiator 4×4", "ארגז + עבירות", Category.PICKUP, 0xFF2F3F4C, 0xFF94C7E8, R.drawable.jeep_gladiator_sahara),
    Jeep(13, "Gladiator Adventure", "מוכן לטיול הבא", Category.PICKUP, 0xFF2F4A40, 0xFF9FE6C0, R.drawable.jeep_gladiator_willys),

    Jeep(14, "Cherokee XJ", "קלאסיקת שטח", Category.OFF_ROAD, 0xFF5B2626, 0xFFFFB36B, R.drawable.jeep_cherokee_xj),
    Jeep(15, "Cherokee 2-Door", "הדור האגדי", Category.OFF_ROAD, 0xFF6C2525, 0xFFFFC56E, R.drawable.jeep_cherokee_2door),
    Jeep(16, "Cherokee KK", "SUV קומפקטי", Category.FAMILY, 0xFF25333D, 0xFFBCE2F5, R.drawable.jeep_cherokee_kk),
    Jeep(17, "Cherokee Trail", "לשביל וליום יום", Category.OFF_ROAD, 0xFF354E42, 0xFFC4F07C, R.drawable.jeep_cherokee_kk),

    Jeep(18, "Grand Cherokee ZJ", "1993–1998 • קלאסיקה", Category.FAMILY, 0xFF1F2A31, 0xFF93B8CF, R.drawable.jeep_grand_cherokee_zj),
    Jeep(19, "Grand Cherokee WJ", "1999–2003 • V8", Category.FAMILY, 0xFF5B2F2A, 0xFFFFC08A, R.drawable.jeep_grand_cherokee_wj),
    Jeep(20, "Grand Cherokee WK", "2005–2007 • SUV גדול", Category.FAMILY, 0xFF2B3038, 0xFFD1DAE5, R.drawable.jeep_grand_cherokee_wk),
    Jeep(21, "Grand Cherokee 4xe", "פלאג־אין מודרני", Category.ELECTRIC, 0xFF36414C, 0xFF75E9D2, R.drawable.jeep_grand_cherokee_4xe),
    Jeep(22, "Grand Cherokee Trailhawk", "נוחות עם יכולת שטח", Category.OFF_ROAD, 0xFF3D423B, 0xFFD9EF8A, R.drawable.jeep_grand_cherokee_wk),

    Jeep(23, "Compass", "קומפקטי והרפתקני", Category.FAMILY, 0xFF2E3B45, 0xFF82C8FF, R.drawable.jeep_compass),
    Jeep(24, "Compass Trailhawk", "קטן וקשוח", Category.OFF_ROAD, 0xFF514A3D, 0xFFFFC857, R.drawable.jeep_compass_white),
    Jeep(25, "Liberty", "SUV קומפקטי אייקוני", Category.FAMILY, 0xFF31453C, 0xFFB6DF9D, R.drawable.jeep_liberty),
    Jeep(26, "Patriot", "קלאסי ואמין", Category.FAMILY, 0xFF41464B, 0xFFF2CA73, R.drawable.jeep_patriot),

    Jeep(27, "Renegade", "אורבני עם נשמה", Category.FAMILY, 0xFF4A4D38, 0xFFD9EE82, R.drawable.jeep_renegade),
    Jeep(28, "Renegade Trailhawk", "קומפקטי לשטח", Category.OFF_ROAD, 0xFF604D3D, 0xFFE5B85C, R.drawable.jeep_renegade),
    Jeep(29, "CJ Classic", "אייקון מהעבר", Category.OFF_ROAD, 0xFF174C89, 0xFF7FC6FF, R.drawable.jeep_cj),
    Jeep(30, "Grand Cherokee 4xe", "חשמלי, שקט והרפתקני", Category.ELECTRIC, 0xFF25333D, 0xFF77E8D7, R.drawable.jeep_grand_cherokee_4xe)
)
