package com.rangerscards.data.objects

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.rangerscards.R

object CampaignMaps {

    enum class Path(val value: String, @StringRes val nameResId: Int, @DrawableRes val iconResId: Int) {
        WOODS("woods", R.string.woods, R.drawable.woods),
        MOUNTAIN_PASS("mountain_pass", R.string.mountain_pass, R.drawable.mountain_pass),
        OLD_GROWTH("old_growth", R.string.old_growth, R.drawable.old_growth),
        LAKESHORE("lakeshore", R.string.lakeshore, R.drawable.lakeshore),
        GRASSLAND("grassland", R.string.grassland, R.drawable.grassland),
        RAVINE("ravine", R.string.ravine, R.drawable.ravine),
        SWAMP("swamp", R.string.swamp, R.drawable.swamp),
        RIVER("river", R.string.river, R.drawable.river);

        companion object {
            fun fromValue(value: String): Path? {
                return values().firstOrNull { it.value == value }
            }
        }
    }

    val campaignCyclesMap by lazy {
        mapOf(
            "core" to R.string.core_cycle
        )
    }

    val startingLocations by lazy {
        mapOf(
            "core" to "lone_tree_station"
        )
    }

    val generalSetsMap by lazy {
        mapOf(
            "general" to MapLocation(
                id = "general",
                nameResId = R.string.general_set_general,
                iconResId = R.drawable.general,
            ),
            "the_valley" to MapLocation(
                id = "the_valley",
                nameResId = R.string.general_set_the_valley,
                iconResId = R.drawable.the_valley
            )
        )
    }

    private val connections by lazy {
        listOf(
            Connection("atrox_mountain", "northern_outpost", Path.WOODS),
            Connection("atrox_mountain", "golden_shore", Path.LAKESHORE),
            Connection("atrox_mountain", "lone_tree_station", Path.MOUNTAIN_PASS),
            Connection("golden_shore", "northern_outpost", Path.WOODS),
            Connection("golden_shore", "mount_nim", Path.LAKESHORE),
            Connection("northern_outpost", "mount_nim", Path.MOUNTAIN_PASS),
            Connection("white_sky", "mount_nim", Path.MOUNTAIN_PASS),
            Connection("white_sky", "lone_tree_station", Path.LAKESHORE),
            Connection("white_sky", "boulder_field", Path.WOODS),
            Connection("lone_tree_station", "boulder_field", Path.WOODS),
            Connection("lone_tree_station", "ancestors_grove", Path.OLD_GROWTH),
            Connection("ancestors_grove", "boulder_field", Path.WOODS),
            Connection("kobos_market", "boulder_field", Path.GRASSLAND),
            Connection("kobos_market", "ancestors_grove", Path.MOUNTAIN_PASS),
            Connection("the_plummet", "ancestors_grove", Path.MOUNTAIN_PASS),
            Connection("the_plummet", "kobos_market", Path.LAKESHORE),
            Connection("the_plummet", "the_concordant_ziggurats", Path.RAVINE),
            Connection("headwaters_station", "the_concordant_ziggurats", Path.RIVER),
            Connection("meadow", "the_concordant_ziggurats", Path.GRASSLAND),
            Connection("meadow", "greenbriar_knoll", Path.GRASSLAND),
            Connection("meadow", "stoneweaver_bridge", Path.GRASSLAND),
            Connection("meadow", "rings_of_the_moon", Path.RAVINE),
            Connection("the_concordant_ziggurats", "rings_of_the_moon", Path.RIVER),
            Connection("the_concordant_ziggurats", "archeological_outpost", Path.GRASSLAND),
            Connection("rings_of_the_moon", "archeological_outpost", Path.RAVINE),
            Connection("rings_of_the_moon", "the_alluvial_ruins", Path.RIVER),
            Connection("archeological_outpost", "the_alluvial_ruins", Path.MOUNTAIN_PASS),
            Connection("archeological_outpost", "watchers_rock", Path.RAVINE),
            Connection("the_tumbledown", "watchers_rock", Path.MOUNTAIN_PASS),
            Connection("kobos_market", "spire", Path.LAKESHORE),
            Connection("white_sky", "spire", Path.RIVER),
            Connection("the_philosophers_garden", "spire", Path.OLD_GROWTH),
            Connection("the_philosophers_garden", "the_fractured_wall", Path.WOODS),
            Connection("mount_nim", "the_fractured_wall", Path.MOUNTAIN_PASS),
            Connection("the_high_basin", "the_fractured_wall", Path.LAKESHORE),
            Connection("the_furrow", "the_fractured_wall", Path.MOUNTAIN_PASS),
            Connection("the_high_basin", "the_furrow", Path.LAKESHORE),
            Connection("the_high_basin", "branch", Path.OLD_GROWTH),
            Connection("the_philosophers_garden", "branch", Path.OLD_GROWTH),
            Connection("crossroads_station", "branch", Path.OLD_GROWTH),
            Connection("crossroads_station", "spire", Path.GRASSLAND),
            Connection("terravore", "the_furrow", Path.RAVINE),
            Connection("crossroads_station", "biologists_outpost", Path.OLD_GROWTH),
            Connection("the_high_basin", "biologists_outpost", Path.RIVER),
            Connection("stoneweaver_bridge", "biologists_outpost", Path.RIVER),
            Connection("stoneweaver_bridge", "spire", Path.RIVER),
            Connection("stoneweaver_bridge", "greenbriar_knoll", Path.RAVINE),
            Connection("spire", "greenbriar_knoll", Path.WOODS),
            Connection("biologists_outpost", "mound_of_the_navigator", Path.WOODS),
            Connection("terravore", "mound_of_the_navigator", Path.OLD_GROWTH),
            Connection("stoneweaver_bridge", "mound_of_the_navigator", Path.RAVINE),
            Connection("the_greenbridge", "mound_of_the_navigator", Path.SWAMP),
            Connection("the_greenbridge", "sunken_outpost", Path.SWAMP),
            Connection("michaels_bog", "sunken_outpost", Path.SWAMP),
            Connection("michaels_bog", "the_cypress_citadel", Path.SWAMP),
            Connection("michaels_bog", "the_frowning_gate", Path.SWAMP),
            Connection("sunken_outpost", "the_frowning_gate", Path.SWAMP),
            Connection("the_alluvial_ruins", "the_frowning_gate", Path.RAVINE),
            Connection("bowl_of_the_sun", "the_frowning_gate", Path.MOUNTAIN_PASS),
            Connection("the_alluvial_ruins", "stoneweaver_bridge", Path.RIVER),
            Connection("the_alluvial_ruins", "bowl_of_the_sun", Path.RAVINE),
            Connection("the_tumbledown", "bowl_of_the_sun", Path.MOUNTAIN_PASS),
            Connection("the_tumbledown", "the_alluvial_ruins", Path.RIVER),
        )
    }

    private val paths by lazy {
        listOf(
            MapLocation("atrox_mountain", R.string.atrox_mountain, R.drawable.atrox_mountain),
            MapLocation("northern_outpost", R.string.northern_outpost, R.drawable.northern_outpost),
            MapLocation("lone_tree_station", R.string.lone_tree_station, R.drawable.lone_tree_station),
            MapLocation("white_sky", R.string.white_sky, R.drawable.white_sky),
            MapLocation("golden_shore", R.string.golden_shore, R.drawable.golden_shore),
            MapLocation("mount_nim", R.string.mount_nim, R.drawable.mount_nim),
            MapLocation("ancestors_grove", R.string.ancestors_grove, R.drawable.ancestors_grove),
            MapLocation("kobos_market", R.string.kobos_market, R.drawable.kobos_market),
            MapLocation("boulder_field", R.string.boulder_field, R.drawable.boulder_field),
            MapLocation("the_fractured_wall", R.string.the_fractured_wall, R.drawable.the_fractured_wall),
            MapLocation("the_philosophers_garden", R.string.the_philosophers_garden, R.drawable.the_philosophers_garden),
            MapLocation("the_high_basin", R.string.the_high_basin, R.drawable.the_high_basin),
            MapLocation("branch", R.string.branch, R.drawable.branch),
            MapLocation("spire", R.string.spire, R.drawable.spire),
            MapLocation("crossroads_station", R.string.crossroads_station, R.drawable.crossroads_station),
            MapLocation("the_furrow", R.string.the_furrow, R.drawable.the_furrow),
            MapLocation("biologists_outpost", R.string.biologists_outpost, R.drawable.biologists_outpost),
            MapLocation("terravore", R.string.terravore, R.drawable.terravore),
            MapLocation("mound_of_the_navigator", R.string.mound_of_the_navigator, R.drawable.mound_of_the_navigator),
            MapLocation("the_greenbridge", R.string.the_greenbridge, R.drawable.the_greenbridge),
            MapLocation("michaels_bog", R.string.michaels_bog, R.drawable.michaels_bog),
            MapLocation("the_cypress_citadel", R.string.the_cypress_citadel, R.drawable.the_cypress_citadel),
            MapLocation("marsh_of_rebirth", R.string.marsh_of_rebirth, R.drawable.marsh_of_rebirth),
            MapLocation("sunken_outpost", R.string.sunken_outpost, R.drawable.sunken_outpost),
            MapLocation("the_frowning_gate", R.string.the_frowning_gate, R.drawable.the_frowning_gate),
            MapLocation("bowl_of_the_sun", R.string.bowl_of_the_sun, R.drawable.bowl_of_the_sun),
            MapLocation("the_alluvial_ruins", R.string.the_alluvial_ruins, R.drawable.the_alluvial_ruins),
            MapLocation("the_tumbledown", R.string.the_tumbledown, R.drawable.the_tumbledown),
            MapLocation("watchers_rock", R.string.watchers_rock, R.drawable.watchers_rock),
            MapLocation("archeological_outpost", R.string.archeological_outpost, R.drawable.archeological_outpost),
            MapLocation("rings_of_the_moon", R.string.rings_of_the_moon, R.drawable.rings_of_the_moon),
            MapLocation("the_concordant_ziggurats", R.string.the_concordant_ziggurats, R.drawable.the_concordant_ziggurats),
            MapLocation("meadow", R.string.meadow, R.drawable.meadow),
            MapLocation("stoneweaver_bridge", R.string.stoneweaver_bridge, R.drawable.stoneweaver_bridge),
            MapLocation("greenbriar_knoll", R.string.greenbriar_knoll, R.drawable.greenbriar_knoll),
            MapLocation("the_plummet", R.string.the_plummet, R.drawable.the_plummet),
            MapLocation("headwaters_station", R.string.headwaters_station, R.drawable.headwaters_station)
        )
    }

    fun getMapLocations(needConnections: Boolean): Map<String, MapLocation> {
        val results = mutableMapOf<String, MapLocation>()
        paths.forEach {
            results[it.id] = it
        }
        if (needConnections) connections.forEach { connection ->
            val locA = results[connection.locA]
            val locB = results[connection.locB]
            if (locA != null && locB != null) {
                locA.connections.add(MapConnection(connection.locB, connection.path))
                locB.connections.add(MapConnection(connection.locA, connection.path))
            }
        }
        return results
    }

    val fixedGuideEntries by lazy {
        mapOf(
            "core" to mapOf(
                1 to listOf("1"),
                3 to listOf("94.1"),
                4 to listOf("1.04"),
            )
        )
    }

    val weather by lazy {
        mapOf(
            "core" to listOf(
                Weather(1, 3, R.string.weather_perfect_day),
                Weather(4, 7, R.string.weather_downpour),
                Weather(8, 9, R.string.weather_perfect_day),
                Weather(10, 12, R.string.weather_downpour),
                Weather(13, 14, R.string.weather_howling_winds),
                Weather(15, 17, R.string.weather_downpour),
                Weather(18, 20, R.string.weather_howling_winds),
                Weather(21, 22, R.string.weather_perfect_day),
                Weather(23, 25, R.string.weather_downpour),
                Weather(26, 28, R.string.weather_howling_winds),
                Weather(29, 30, R.string.weather_perfect_day),
            )
        )
    }
}

data class Connection(
    val locA: String,
    val locB: String,
    val path: CampaignMaps.Path
)

data class MapConnection(
    val id: String,
    val path: CampaignMaps.Path
)

data class MapLocation(
    val id: String,
    @StringRes val nameResId: Int,
    @DrawableRes val iconResId: Int,
    val cycles: List<String> = listOf("core"),
    val connections: MutableList<MapConnection> = mutableListOf()
)

data class Weather(
    val start: Int,
    val end: Int,
    @StringRes val nameResId: Int
)