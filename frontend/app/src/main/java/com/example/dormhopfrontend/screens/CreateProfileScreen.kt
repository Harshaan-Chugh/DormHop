/*  ui/ CreateProfileScreen.kt  */
package com.example.dormhopfrontend.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dormhopfrontend.R
import com.example.dormhopfrontend.ui.theme.GoldAccent
import com.example.dormhopfrontend.ui.theme.RedPrimary
import com.example.dormhopfrontend.viewmodel.CreateProfileViewModel
import com.example.dormhopfrontend.viewmodel.UiState


@Composable
fun CreateProfileScreen(
    viewModel: CreateProfileViewModel = hiltViewModel(),
    onDone: () -> Unit = {}
) {
    val ui by viewModel.uiState.collectAsState()
    LaunchedEffect(ui.done) {
        if (ui.done) onDone()
    }

    Scaffold(
        topBar = { StepTopBar(ui.step) },
        bottomBar = { BottomButtons(ui, viewModel) }
    ) { inner ->
        when (ui.step) {
            1 -> PersonalStep(ui, viewModel, Modifier.padding(inner))
            2 -> DormStep(ui, viewModel, Modifier.padding(inner))
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StepTopBar(step: Int) = TopAppBar(
    title = { Text(if (step == 1) "Your details" else "Dorm details") }
)

@Composable
private fun BottomButtons(ui: UiState, vm: CreateProfileViewModel) {
    when (ui.step) {
        1 -> {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                val enabled = ui.canProceedPersonal
                Button(
                    onClick = vm::onNextPersonal,
                    enabled = enabled
                ) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "Next")
                    Spacer(Modifier.width(8.dp))
                    Text("Next")
                }
            }
        }
        2 -> {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(onClick = vm::onBackToPersonal) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    Spacer(Modifier.width(8.dp))
                    Text("Back")
                }
                val enabled = ui.canProceedDorm && !ui.saving
                Button(
                    onClick = vm::onSave,
                    enabled = enabled
                ) {
                    if (ui.saving) {
                        CircularProgressIndicator(
                            Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = LocalContentColor.current
                        )
                    } else {
                        Icon(Icons.Default.Save, contentDescription = "Save")
                        Spacer(Modifier.width(8.dp))
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
private fun PersonalStep(
    ui: UiState,
    vm: CreateProfileViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = ui.fullName,
            onValueChange = vm::onFullNameChanged,
            label = { Text("Full name (name will not be displayed)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = ui.email,
            onValueChange = vm::onEmailChanged,
            label = { Text("Cornell email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        OutlinedTextField(
            value = ui.classYear ?: "",
            onValueChange = vm::onClassYearChanged,
            label = { Text("Graduation/Class year") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = ui.isRoomListed,
                onCheckedChange = vm::onIsRoomListedChanged
            )
            Text("Make my room visible in search")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DormStep(
    ui: UiState,
    vm: CreateProfileViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = ui.dorm,
            onValueChange = vm::onDormChanged,
            label = { Text("Dorm name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = ui.roomNumber,
            onValueChange = vm::onRoomNumberChanged,
            label = { Text("Room number") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OccupancyDropdown(
            selected = ui.occupancy,
            onSelected = vm::onOccupancyChanged
        )
        AmenitiesEditor(
            amenities = ui.amenities,
            onAdd = vm::addAmenity,
            onRemove = vm::removeAmenity
        )
        OutlinedTextField(
            value = ui.description,
            onValueChange = vm::onDescriptionChanged,
            label = { Text("Description (recommended)") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OccupancyDropdown(
    selected: String?,
    onSelected: (String?) -> Unit
) {
    val options = listOf("Single", "Double", "Triple", "Quad")
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selected ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Occupancy") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { opt ->
                DropdownMenuItem(
                    text = { Text(opt) },
                    onClick = {
                        onSelected(opt)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AmenitiesEditor(
    amenities: List<String>,
    onAdd: (String) -> Unit,
    onRemove: (String) -> Unit
) {
    var newAmenity by remember { mutableStateOf("") }

    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        amenities.forEach { tag ->
            AssistChip(
                label = { Text(tag) },
                onClick = { onRemove(tag) },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove $tag",
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }
    }

    // add-new field
    OutlinedTextField(
        value = newAmenity,
        onValueChange = { newAmenity = it },
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Add amenity") },
        trailingIcon = {
            IconButton(
                enabled = newAmenity.isNotBlank(),
                onClick = {
                    onAdd(newAmenity.trim())
                    newAmenity = ""
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add amenity")
            }
        }
    )
}