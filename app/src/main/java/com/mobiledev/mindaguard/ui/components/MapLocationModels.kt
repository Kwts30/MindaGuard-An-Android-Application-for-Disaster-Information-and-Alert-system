package com.mobiledev.mindaguard.ui.components
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
// Shared model
data class MapLocation(
    val id: String,
    val name: String,
    val address: String,
    val lat: Double,
    val lng: Double,
    val distanceText: String = ""
)
// Evacuation Centers - Davao City (schools & covered courts)
val EvacCenters: List<MapLocation> = listOf(
    // Elementary Schools
    MapLocation("evac_es_01", "Magallanes Elementary School", "Poblacion, Davao City", 7.0637216, 125.6074066),
    MapLocation("evac_es_02", "Sta. Ana Central Elementary School", "Poblacion, Davao City", 7.0735888, 125.6192896),
    MapLocation("evac_es_03", "Kapitan Tomas Monteverde Sr. Central ES", "Poblacion, Davao City", 7.0706711, 125.6099366),
    MapLocation("evac_es_04", "Teodoro L. Palma Gil Elementary School", "Poblacion, Davao City", 7.0724260, 125.6085960),
    MapLocation("evac_es_05", "Manuel A. Roxas Elementary School", "Poblacion, Davao City", 7.0714694, 125.6190345),
    MapLocation("evac_es_06", "Jose P. Rizal Elementary School", "Poblacion, Davao City", 7.0723033, 125.6188926),
    MapLocation("evac_es_07", "Manuel L. Quezon Elementary School", "Poblacion, Davao City", 7.0713131, 125.6190456),
    MapLocation("evac_es_08", "Wireless Elementary School", "Poblacion, Brgy 6-A, Davao City", 7.0724657, 125.6010876),
    MapLocation("evac_es_09", "Jose L. Porras Elementary School", "Agdao, Davao City", 7.0907194, 125.6259900),
    MapLocation("evac_es_10", "SIR Elementary School", "Bucana, Matina, Davao City", 7.0598871, 125.6025453),
    MapLocation("evac_es_11", "Calinan Central Elementary School", "Calinan District, Davao City", 7.1867157, 125.4547597),
    MapLocation("evac_es_12", "Lacson Elementary School", "Calinan District, Davao City", 7.2111330, 125.4427520),
    MapLocation("evac_es_13", "Lorenzo Elementary School", "Calinan District, Davao City", 7.2072980, 125.4914030),
    MapLocation("evac_es_14", "Pangyan Elementary School", "Calinan District, Davao City", 7.2062129, 125.5028042),
    // High Schools
    MapLocation("evac_hs_01", "Davao City National High School (Main)", "Poblacion, Davao City", 7.0790428, 125.6063331),
    MapLocation("evac_hs_02", "Davao City National HS - Madapo Campus", "Poblacion, Davao City", 7.0762756, 125.5956684),
    MapLocation("evac_hs_03", "Davao City Special National High School", "Km.7 Bangkal, Talomo, Davao City", 7.0616205, 125.5604084),
    MapLocation("evac_hs_04", "Sta. Ana National HS (Main Campus)", "Poblacion, Davao City", 7.0722449, 125.6196757),
    MapLocation("evac_hs_05", "Sta. Ana National HS (Annex)", "Poblacion, Davao City", 7.0721201, 125.6168917),
    MapLocation("evac_hs_06", "Carlos P. Garcia Senior High School", "Poblacion, Davao City", 7.0726247, 125.6201468),
    MapLocation("evac_hs_07", "Davao Christian High School - V. Mapa", "Poblacion, Davao City", 7.0794606, 125.6107688),
    MapLocation("evac_hs_08", "Davao Chong Hua High School", "Poblacion, Davao City", 7.0770102, 125.6136917),
    MapLocation("evac_hs_09", "Maa National High School", "Talomo District, Davao City", 7.0898670, 125.5784484),
    MapLocation("evac_hs_10", "Don Enrique Bustamante National HS", "Talomo District, Davao City", 7.0887412, 125.6179329),
    MapLocation("evac_hs_11", "Vicenta C. Nograles National High School", "Bucana, Matina, Davao City", 7.0602798, 125.6028256),
    MapLocation("evac_hs_12", "Ateneo de Davao Senior High School", "McArthur Hwy, Talomo, Davao City", 7.0599333, 125.5568749),
    MapLocation("evac_hs_13", "Cabantian National High School", "Buhangin District, Davao City", 7.1295902, 125.6211056),
    MapLocation("evac_hs_14", "Waan National High School", "Buhangin District, Davao City", 7.1326694, 125.5763856),
    MapLocation("evac_hs_15", "Calinan National High School", "Calinan District, Davao City", 7.1847030, 125.4566270),
    MapLocation("evac_hs_16", "Subasta National High School", "Calinan District, Davao City", 7.1480345, 125.4378917),
    // Covered Courts
    MapLocation("evac_cc_01", "Barangay 7-A Covered Court", "Poblacion, Brgy 7-A, Davao City", 7.0732786, 125.6038399),
    MapLocation("evac_cc_02", "Barangay 21-C Covered Basketball Court", "Poblacion, Brgy 21-C, Davao City", 7.0671582, 125.6193766),
    MapLocation("evac_cc_03", "Barangay 23 Covered Court", "Poblacion, Brgy 23, Davao City", 7.0716353, 125.6220853),
    MapLocation("evac_cc_04", "Barangay 5-A Covered Court", "Datu Bago, Brgy 5-A, Davao City", 7.0700110, 125.5989684),
    MapLocation("evac_cc_05", "Barangay 40-D Covered Basketball Court", "Talomo, Brgy 40-D, Davao City", 7.0587090, 125.6100355),
    MapLocation("evac_cc_06", "Bangkaan Brgy 1 Covered Court", "Talomo, Brgy 1, Davao City", 7.0639650, 125.6036535),
    MapLocation("evac_cc_07", "Gravahan Covered Court", "Talomo District, Davao City", 7.0655239, 125.5989217),
    MapLocation("evac_cc_08", "Talomo Cemento Covered Court", "Talomo District, Davao City", 7.0466196, 125.5521715),
    MapLocation("evac_cc_09", "IWHA Village Covered Court", "Talomo District, Davao City", 7.0366694, 125.5094748),
    MapLocation("evac_cc_10", "Ecoland Subd. Phase 1 Covered Court", "Matina, Ecoland, Davao City", 7.0511483, 125.5946845),
    MapLocation("evac_cc_11", "Davao Executive Homes Covered Court", "Talomo District, Davao City", 7.0542145, 125.5802760),
    MapLocation("evac_cc_12", "Dusnai Covered Court", "Talomo / Toril Area, Davao City", 7.0346338, 125.5251230),
    MapLocation("evac_cc_13", "Crossing Bayabas Covered Court", "Toril District, Davao City", 7.0231956, 125.4951283),
    MapLocation("evac_cc_14", "Barangay Duterte Covered Court", "Gov. Vicente Duterte, Davao City", 7.0895535, 125.6276735),
    MapLocation("evac_cc_15", "Cory Village Covered Court", "Agdao / Buhangin, Davao City", 7.0923341, 125.6178833),
    MapLocation("evac_cc_16", "San Antonio Covered Court", "Agdao District, Davao City", 7.0922023, 125.6297155),
    MapLocation("evac_cc_17", "NHA Covered Court", "Bajada District, Davao City", 7.1134632, 125.6248852),
    MapLocation("evac_cc_18", "Covered Court Km.8, Brgy. Tigatto", "Buhangin District, Davao City", 7.1205627, 125.5986813),
    MapLocation("evac_cc_19", "Covered Court Uyanguren, Brgy. Tigatto", "Buhangin District, Davao City", 7.1141373, 125.5907654)
)
// Critical Facilities - Davao City hospitals & barangay health centers
private val Hospitals: List<MapLocation> = listOf(
    MapLocation("hosp_01", "Davao Doctors Hospital", "Elpidio Quirino Ave., Poblacion, Davao City", 7.0702845, 125.6047170),
    MapLocation("hosp_02", "Southern Philippines Medical Center (SPMC)", "J.P. Laurel Ave., Bajada, Davao City", 7.0983727, 125.6198369),
    MapLocation("hosp_03", "San Pedro Hospital of Davao City", "Guzman St., Poblacion, Davao City", 7.0790356, 125.6149564),
    MapLocation("hosp_04", "Metro Davao Medical & Research Center", "J.P. Laurel Ave., Poblacion, Davao City", 7.0950317, 125.6132555),
    MapLocation("hosp_05", "Brokenshire Medical Center", "Brokenshire Dr., Madapo, Poblacion, Davao City", 7.0739669, 125.5983517),
    MapLocation("hosp_06", "Ricardo Limso Medical Center", "Ilustre St., Poblacion, Davao City", 7.0705024, 125.6068573),
    MapLocation("hosp_07", "Anda Riverview Medical Center", "Magallanes St., Poblacion, Davao City", 7.0651484, 125.6055039),
    MapLocation("hosp_08", "Adventist Hospital Davao", "Km. 7 Central Park Blvd., Talomo, Davao City", 7.0601280, 125.5550516),
    MapLocation("hosp_09", "United Davao Specialists Hospital", "Km. 4 McArthur Hwy., Ma-a, Talomo, Davao City", 7.0626620, 125.5903772),
    MapLocation("hosp_10", "Lanang Premiere Doctors Hospital (LPDHI)", "Dacudao Loop, Agdao, Davao City", 7.0973874, 125.6330686),
    MapLocation("hosp_11", "Alterado General Hospital", "R. Castillo St., Agdao, Davao City", 7.0869287, 125.6320453),
    MapLocation("hosp_12", "GIG Oca Robles Seamen's Hospital (AMOSUP)", "R. Castillo St., Agdao, Davao City", 7.0948215, 125.6377403),
    MapLocation("hosp_13", "Isaac T. Robillo Hospital Corporation", "Km. 26 Davao-Bukidnon Rd., Calinan, Davao City", 7.1778130, 125.4666947),
    MapLocation("hosp_14", "Ernesto Guadalupe Community Hospital", "Jasmin St., Toril, Davao City", 7.0095607, 125.5029289),
    MapLocation("hosp_15", "Davao Mediquest Hospital", "Toril District, Davao City", 7.0114372, 125.4895749)
)
private val BarangayHealthCenters: List<MapLocation> = listOf(
    MapLocation("bhc_01", "Davao City Health Office (Main)", "124 Pichon St., Poblacion, Davao City", 7.0627934, 125.6093287),
    MapLocation("bhc_02", "Barangay 4-A Health Center", "Pelayo St., Poblacion, Davao City", 7.0714651, 125.6070907),
    MapLocation("bhc_03", "Barangay 14-B Health Center", "Buyayang St., Poblacion, Davao City", 7.0782621, 125.6167950),
    MapLocation("bhc_04", "Barangay 17-B Health Center", "Nidea St., Obrero, Davao City", 7.0847915, 125.6168798),
    MapLocation("bhc_05", "Barangay 19-B Health Center", "Bacaca Road, Poblacion, Davao City", 7.0906575, 125.6065697),
    MapLocation("bhc_06", "Barangay 20-B Health Center", "Porras Veloso St., Obrero, Davao City", 7.0871703, 125.6131411),
    MapLocation("bhc_07", "Barangay 37-D Health Center", "Talomo District, Davao City", 7.0634652, 125.6139261),
    MapLocation("bhc_08", "Barangay 38-D Health Center", "J.P. Rizal St., Poblacion, Davao City", 7.0644833, 125.6113759),
    MapLocation("bhc_09", "Barangay Health Center (Quezon St.)", "Aurora, Quezon St., Poblacion, Davao City", 7.0673795, 125.6178744),
    MapLocation("bhc_10", "Talomo North Health Center", "Daang Patnubay, Talomo, Davao City", 7.0631314, 125.6030102),
    MapLocation("bhc_11", "Talomo Central Health Center", "Libra St., Talomo, Davao City", 7.0593566, 125.5743967),
    MapLocation("bhc_12", "Gravahan Matina Health Center", "Gravahan, New Matina, Davao City", 7.0646935, 125.5983747),
    MapLocation("bhc_13", "Matina Aplaya Health Center", "Talomo District, Davao City", 7.0449283, 125.5681947),
    MapLocation("bhc_14", "Barangay Ma-a Health Center", "Don Julian Rodriguez Ave., Talomo, Davao City", 7.0900001, 125.5798976),
    MapLocation("bhc_15", "Barangay Catalunan Pequeno Health Center", "Talomo District, Davao City", 7.0759470, 125.5181453),
    MapLocation("bhc_16", "Barangay Health Center (Langub Rd.)", "Langub Rd., Talomo, Davao City", 7.1057348, 125.5629327),
    MapLocation("bhc_17", "Agdao Health Center", "Lapu-Lapu St., Agdao, Davao City", 7.0811802, 125.6221175),
    MapLocation("bhc_18", "Barangay Centro Health Center", "Agdao District, Davao City", 7.0897572, 125.6396312),
    MapLocation("bhc_19", "Barangay Gov. Vicente Duterte Health Center", "Barangay Hall, Agdao, Davao City", 7.0895138, 125.6278057),
    MapLocation("bhc_20", "Barangay Ubalde Health Center", "Lakandula St., Agdao, Davao City", 7.0951174, 125.6346517),
    MapLocation("bhc_21", "R. Castillo Barangay Health Center", "Agdao District, Davao City", 7.1005261, 125.6354879),
    MapLocation("bhc_22", "Buhangin Health Center", "NHA Chapel St., Buhangin, Davao City", 7.1138593, 125.6248024),
    MapLocation("bhc_23", "Cabantian Barangay Health Center Annex", "Km. 10 Cabantian Rd., Buhangin, Davao City", 7.1378141, 125.6069231),
    MapLocation("bhc_24", "Waan Barangay Health Center", "Buhangin District, Davao City", 7.1270437, 125.5754222),
    MapLocation("bhc_25", "Calinan Health Center", "Calinan District, Davao City", 7.1891786, 125.4604692),
    MapLocation("bhc_26", "Jacinto Barangay Health Center", "Jacinto St., Calinan, Davao City", 7.1895600, 125.4559220),
    MapLocation("bhc_27", "Tamayong Barangay Health Center", "Calinan District, Davao City", 7.1323026, 125.3795120),
    MapLocation("bhc_28", "Toril Health Center", "Agton St., Toril, Davao City", 7.0198198, 125.4973253),
    MapLocation("bhc_29", "Toril Urban Health Center (Toril-B)", "Juan de la Cruz St., Toril, Davao City", 7.0116640, 125.5018949),
    MapLocation("bhc_30", "Barangay Health Center of North Daliao", "Prudential Village, Toril, Davao City", 7.0161639, 125.5108094),
    MapLocation("bhc_31", "Barangay Health Center (Tugbok)", "Purok 2, Tugbok District, Davao City", 7.0785074, 125.4953063)
)
// Combined list consumed by the rest of the app (46 total)
val CriticalFacilities: List<MapLocation> = Hospitals + BarangayHealthCenters
// Reusable list-item UI
@Composable
fun LocationListItem(
    location: MapLocation,
    pinColor: Color = Color(0xFF4CAF50),
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        color = Color.White,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = null,
                tint = pinColor,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = location.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = location.address,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.Gray
                )
            }
        }
    }
}