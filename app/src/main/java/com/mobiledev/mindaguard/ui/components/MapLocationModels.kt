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

// ── Shared model ─────────────────────────────────────────────────────────────
data class MapLocation(
    val id: String,
    val name: String,
    val address: String,
    val lat: Double,
    val lng: Double,
    val distanceText: String = ""
)

// ── Evacuation Centers — Davao City (schools, gyms, covered courts) ──────────
val EvacCenters: List<MapLocation> = listOf(
    // District 1 – Poblacion / Agdao / Buhangin
    MapLocation("evac_001","Davao City National High School","R. Magsaysay Ave., Poblacion, Davao City",7.0643,125.6075),
    MapLocation("evac_002","San Pedro College Gym","Assumption Rd., Davao City",7.0652,125.6102),
    MapLocation("evac_003","Agdao Elementary School","F. Torres St., Agdao, Davao City",7.0808,125.6148),
    MapLocation("evac_004","Agdao Covered Court","Agdao, Davao City",7.0801,125.6141),
    MapLocation("evac_005","Buhangin Elementary School","Buhangin, Davao City",7.1023,125.6320),
    MapLocation("evac_006","Buhangin National High School","Buhangin, Davao City",7.1030,125.6335),
    MapLocation("evac_007","Panacan Elementary School","Panacan, Davao City",7.1194,125.6423),
    MapLocation("evac_008","Sasa Elementary School","Sasa, Davao City",7.1050,125.6400),
    MapLocation("evac_009","Bunawan Covered Court","Bunawan, Davao City",7.0927,125.5895),
    MapLocation("evac_010","Bunawan Elementary School","Bunawan, Davao City",7.0935,125.5905),
    // District 2 – Calinan / Tugbok / Matina
    MapLocation("evac_011","Calinan National High School","Calinan, Davao City",7.1843,125.3953),
    MapLocation("evac_012","Calinan Elementary School","Calinan, Davao City",7.1838,125.3940),
    MapLocation("evac_013","Tugbok Elementary School","Tugbok, Davao City",7.1202,125.5400),
    MapLocation("evac_014","Matina Elementary School","Matina, Davao City",7.0510,125.5850),
    MapLocation("evac_015","Matina National High School Gym","Matina, Davao City",7.0502,125.5862),
    MapLocation("evac_016","Mintal Elementary School","Mintal, Davao City",7.1455,125.4652),
    MapLocation("evac_017","Catalunan Grande Covered Court","Catalunan Grande, Davao City",7.0350,125.5530),
    MapLocation("evac_018","Catalunan Pequeño Gym","Catalunan Pequeño, Davao City",7.0365,125.5512),
    MapLocation("evac_019","New Valencia Covered Court","New Valencia, Davao City",7.1120,125.5100),
    MapLocation("evac_020","Indangan Elem School","Indangan, Davao City",7.1260,125.6000),
    // District 3 – Talomo / Toril / Baguio
    MapLocation("evac_021","Talomo Elementary School","Talomo, Davao City",7.0720,125.6010),
    MapLocation("evac_022","Ma-a Elementary School","Barangay Ma-a, Talomo, Davao City",7.0713,125.6075),
    MapLocation("evac_023","Ma-a Covered Court","Barangay Ma-a, Talomo, Davao City",7.0721,125.6090),
    MapLocation("evac_024","Toril National High School","Toril, Davao City",6.9973,125.5250),
    MapLocation("evac_025","Toril Elementary School Gym","Toril, Davao City",6.9965,125.5238),
    MapLocation("evac_026","Binugao Covered Court","Binugao, Davao City",7.0020,125.5190),
    MapLocation("evac_027","Baliok Elementary School","Baliok, Davao City",7.0050,125.5350),
    MapLocation("evac_028","Eden Elementary School","Eden, Davao City",7.1820,125.4552),
    MapLocation("evac_029","Sirawan Covered Court","Sirawan, Davao City",6.9852,125.5650),
    MapLocation("evac_030","Ula Elementary School","Ula, Davao City",7.0200,125.4800),
    // District 4 – Poblacion (city center) / Lanang / Maa / Matina
    MapLocation("evac_031","Davao City Recreation Center","CM Recto St., Davao City",7.0638,125.6042),
    MapLocation("evac_032","San Beda College Gym (PhilSports Annex)","Lanang, Davao City",7.0895,125.6280),
    MapLocation("evac_033","UM Gym","Bolton St., Davao City",7.0620,125.6030),
    MapLocation("evac_034","Davao del Sur Capitol Grounds","Bonifacio St., Davao City",7.0660,125.6093),
    MapLocation("evac_035","Maa Elementary School","Maa, Davao City",7.0480,125.5975),
    MapLocation("evac_036","Maa Covered Court","Maa, Davao City",7.0490,125.5982),
    MapLocation("evac_037","Bangkas Heights Covered Court","Bangkas Heights, Davao City",7.0420,125.5840),
    MapLocation("evac_038","Bucana Elementary School","Bucana, Davao City",7.0730,125.6230),
    MapLocation("evac_039","Dumoy Elementary School","Dumoy, Davao City",7.0562,125.5760),
    MapLocation("evac_040","Diversion Road Covered Court","Maa-Diversion, Davao City",7.0555,125.6050),
    // Poblacion barangays
    MapLocation("evac_041","Barangay 1 Multipurpose Hall","Poblacion, Davao City",7.0645,125.6062),
    MapLocation("evac_042","Barangay 23 Covered Court","Poblacion, Davao City",7.0630,125.6080),
    MapLocation("evac_043","Leon Garcia Elementary School","Leon Garcia, Davao City",7.0810,125.6180),
    MapLocation("evac_044","Pampanga Covered Court","Pampanga, Davao City",7.0730,125.5905),
    MapLocation("evac_045","Lapu-Lapu Covered Court","Lapu-Lapu, Davao City",7.0905,125.6060),
    MapLocation("evac_046","Uyanguren Gym","Uyanguren, Davao City",7.0670,125.6130),
    MapLocation("evac_047","Lacson Elementary School","Lacson, Davao City",7.0985,125.6050),
    MapLocation("evac_048","Tibungco Elementary School","Tibungco, Davao City",7.1170,125.6350),
    MapLocation("evac_049","Communal Elementary School","Communal, Davao City",7.1070,125.6290),
    MapLocation("evac_050","Waan Covered Court","Waan, Davao City",7.1012,125.5780)
)

// ── Critical Facilities — hospitals & barangay health centers ────────────────
val CriticalFacilities: List<MapLocation> = listOf(
    // Major Hospitals
    MapLocation("crit_001","Southern Philippines Medical Center (SPMC)","JP Laurel Ave., Bajada, Davao City",7.0726,125.6144),
    MapLocation("crit_002","Davao Medical School Foundation Hospital","Airport Rd., Bajada, Davao City",7.0745,125.6160),
    MapLocation("crit_003","San Pedro Hospital","Pichon St., Davao City",7.0655,125.6038),
    MapLocation("crit_004","Davao Doctors Hospital","E. Quirino Ave., Davao City",7.0648,125.6052),
    MapLocation("crit_005","Brokenshire College Hospital","Madapo Hills, Davao City",7.0755,125.6055),
    MapLocation("crit_006","Manuel J. Santos Hospital (MJSH)","CM Recto St., Davao City",7.0622,125.6035),
    MapLocation("crit_007","Davao Regional Medical Center (Apoapoaan)","Apoaporaan, Davao City",7.0780,125.5940),
    MapLocation("crit_008","DavaoCare Hospital and Medical Center","MacArthur Hwy., Matina, Davao City",7.0520,125.5870),
    MapLocation("crit_009","Clinica Hilario","Ilustre St., Davao City",7.0663,125.6072),
    MapLocation("crit_010","The Medical City Davao","J.P. Laurel Ave., Bajada, Davao City",7.0720,125.6150),
    // District Health / City Health Centers
    MapLocation("crit_011","Davao City Health Office – Main","Quirino Ave., Davao City",7.0640,125.6048),
    MapLocation("crit_012","Agdao Health Center","Agdao, Davao City",7.0812,125.6152),
    MapLocation("crit_013","Buhangin Health Center","Buhangin, Davao City",7.1020,125.6325),
    MapLocation("crit_014","Bunawan Health Center","Bunawan, Davao City",7.0930,125.5900),
    MapLocation("crit_015","Panacan Health Center","Panacan, Davao City",7.1188,125.6418),
    MapLocation("crit_016","Sasa Health Center","Sasa, Davao City",7.1048,125.6395),
    MapLocation("crit_017","Talomo Health Center","Talomo, Davao City",7.0715,125.6020),
    MapLocation("crit_018","Ma-a Barangay Health Center","Barangay Ma-a, Talomo, Davao City",7.0718,125.6068),
    MapLocation("crit_019","Matina Health Center","Matina, Davao City",7.0505,125.5855),
    MapLocation("crit_020","Toril Health Center","Toril, Davao City",6.9968,125.5242),
    MapLocation("crit_021","Calinan Health Center","Calinan, Davao City",7.1840,125.3948),
    MapLocation("crit_022","Tugbok Health Center","Tugbok, Davao City",7.1205,125.5395),
    MapLocation("crit_023","Mintal Health Center","Mintal, Davao City",7.1452,125.4648),
    MapLocation("crit_024","Catalunan Grande Health Center","Catalunan Grande, Davao City",7.0355,125.5535),
    MapLocation("crit_025","Maa Health Center","Maa, Davao City",7.0482,125.5978),
    MapLocation("crit_026","Bucana Health Center","Bucana, Davao City",7.0728,125.6225),
    MapLocation("crit_027","Pampanga Health Center","Pampanga, Davao City",7.0735,125.5910),
    MapLocation("crit_028","Indangan Health Center","Indangan, Davao City",7.1258,125.5998),
    MapLocation("crit_029","Tibungco Health Center","Tibungco, Davao City",7.1168,125.6348),
    MapLocation("crit_030","Sirawan Health Center","Sirawan, Davao City",6.9855,125.5648),
    MapLocation("crit_031","Communal Health Center","Communal, Davao City",7.1068,125.6288),
    MapLocation("crit_032","Waan Health Center","Waan, Davao City",7.1010,125.5775),
    MapLocation("crit_033","Lacson Health Center","Lacson, Davao City",7.0982,125.6048),
    MapLocation("crit_034","Leon Garcia Health Center","Leon Garcia, Davao City",7.0808,125.6175),
    MapLocation("crit_035","Lapu-Lapu Health Center","Lapu-Lapu, Davao City",7.0902,125.6058),
    MapLocation("crit_036","Uyanguren Health Center","Uyanguren, Davao City",7.0668,125.6128),
    MapLocation("crit_037","Dumoy Health Center","Dumoy, Davao City",7.0560,125.5758),
    MapLocation("crit_038","Eden Health Center","Eden, Davao City",7.1818,125.4548),
    MapLocation("crit_039","Baliok Health Center","Baliok, Davao City",7.0048,125.5348),
    MapLocation("crit_040","Binugao Health Center","Binugao, Davao City",7.0018,125.5188),
    MapLocation("crit_041","Ula Health Center","Ula, Davao City",7.0198,125.4798),
    MapLocation("crit_042","New Valencia Health Center","New Valencia, Davao City",7.1118,125.5098),
    MapLocation("crit_043","Bangkas Heights Health Center","Bangkas Heights, Davao City",7.0418,125.5838),
    MapLocation("crit_044","North Davao Medical Center (Tagum Road)","Buhangin-Tagum Rd., Davao City",7.1035,125.6340),
    MapLocation("crit_045","Toril District Hospital","Toril, Davao City",6.9975,125.5255)
)

// ── Reusable list-item UI ────────────────────────────────────────────────────
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