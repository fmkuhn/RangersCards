package com.rangerscards.ui.decks

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.rangerscards.R
import com.rangerscards.data.database.card.CardListItemProjection
import com.rangerscards.data.objects.DeckMetaMaps
import com.rangerscards.data.objects.StarterDecks
import com.rangerscards.ui.components.DataPicker
import com.rangerscards.ui.components.RangersTabs
import com.rangerscards.ui.components.SquareButton
import com.rangerscards.ui.decks.components.StarterDeck
import com.rangerscards.ui.settings.UserUIState
import com.rangerscards.ui.settings.components.SettingsBaseCard
import com.rangerscards.ui.theme.CustomTheme
import com.rangerscards.ui.theme.Jost
import kotlinx.coroutines.launch
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

enum class ActiveField {
    FieldOne,
    FieldTwo,
    FieldThree
}

@Composable
fun DeckCreationScreen(
    onCancel: () -> Unit,
    onCreate: (String) -> Unit,
    decksViewModel: DecksViewModel,
    user: UserUIState,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    var isCreating by remember { mutableStateOf(false) }
    var tabIndex by rememberSaveable { mutableIntStateOf(0) }
    var name by rememberSaveable { mutableStateOf("") }
    var isUploading by rememberSaveable { mutableStateOf(false) }
    var selectedStarterDeck by rememberSaveable { mutableIntStateOf(-1) }
    var background by rememberSaveable { mutableStateOf("") }
    var specialty by rememberSaveable { mutableStateOf("") }
    var role by remember { mutableStateOf("" to "") }
    var showDialogPicker by rememberSaveable { mutableStateOf<ActiveField?>(null) }
    val isLegit by remember {
        derivedStateOf {
            selectedStarterDeck >= 0 ||
                    (background.isNotEmpty() && specialty.isNotEmpty() && role.first.isNotEmpty())
        }
    }
    Column(
        modifier = modifier
            .background(CustomTheme.colors.l30)
            .fillMaxSize()
            .padding(
                top = contentPadding.calculateTopPadding(),
                bottom = contentPadding.calculateBottomPadding()
            ),
    ) {
        val coroutine = rememberCoroutineScope()
        if (!isCreating) {
            Column(
                modifier = modifier
                    .background(CustomTheme.colors.l30)
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RangersTabs(
                    listOf(
                        R.string.custom_deck_tab,
                        R.string.starter_deck_tab
                    ),
                    tabIndex
                ) {
                    tabIndex = it
                    if (it == 0) selectedStarterDeck = -1
                }
                OutlinedTextField(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .fillMaxWidth(),
                    value = name,
                    onValueChange = { name = it },
                    label = {
                        Text(text = stringResource(R.string.deck_creation_name_label))
                    },
                    placeholder = {
                        Text(text = stringResource(R.string.deck_creation_name_placeholder))
                    },
                    textStyle = TextStyle(
                        color = CustomTheme.colors.d30,
                        fontFamily = Jost,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        lineHeight = 18.sp,
                    ),
                    singleLine = true,
                    shape = CustomTheme.shapes.small,
                    colors = TextFieldDefaults.colors().copy(
                        focusedIndicatorColor = CustomTheme.colors.m,
                        unfocusedIndicatorColor = CustomTheme.colors.m,
                        unfocusedLabelColor = CustomTheme.colors.d30,
                        focusedLabelColor = CustomTheme.colors.d30,
                        unfocusedPlaceholderColor = CustomTheme.colors.d30,
                        focusedPlaceholderColor = CustomTheme.colors.d30,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
                )
                when (tabIndex) {
                    0 -> {
                        val roles = decksViewModel.getRoles(specialty).collectAsLazyPagingItems()
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DataPicker(
                                onClick = { showDialogPicker = ActiveField.FieldOne },
                                type = R.string.background
                            ) {
                                Text(
                                    text = stringResource(if (background.isEmpty())
                                        R.string.background_placeholder
                                    else DeckMetaMaps.background[background]!!),
                                    color = CustomTheme.colors.d30,
                                    fontFamily = Jost,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 16.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            DataPicker(
                                onClick = { showDialogPicker = ActiveField.FieldTwo },
                                type = R.string.specialty
                            ) {
                                Text(
                                    text = stringResource(if (specialty.isEmpty())
                                        R.string.specialty_placeholder
                                    else DeckMetaMaps.specialty[specialty]!!),
                                    color = CustomTheme.colors.d30,
                                    fontFamily = Jost,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 16.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            AnimatedVisibility(specialty.isNotEmpty()) {
                                DataPicker(
                                    onClick = { showDialogPicker = ActiveField.FieldThree },
                                    type = R.string.role
                                ) {
                                    Text(
                                        text = if (role.first.isEmpty())
                                            stringResource(R.string.role_placeholder)
                                        else role.second,
                                        color = CustomTheme.colors.d30,
                                        fontFamily = Jost,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 16.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                        if (showDialogPicker != null) Dialog(
                            onDismissRequest = { showDialogPicker = null },
                            properties = DialogProperties(
                                dismissOnBackPress = true,
                                dismissOnClickOutside = true,
                                usePlatformDefaultWidth = false
                            )
                        ) {
                            SettingsBaseCard(
                                isDarkTheme = isDarkTheme,
                                labelIdRes = when(showDialogPicker) {
                                    ActiveField.FieldOne -> R.string.background
                                    ActiveField.FieldTwo -> R.string.specialty
                                    else -> R.string.role
                                }
                            ) {
                                LazyColumn {
                                    when(showDialogPicker) {
                                        ActiveField.FieldOne -> DeckMetaMaps.background.forEach { (key, value) ->
                                            item {
                                                Text(
                                                    text = stringResource(value),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable {
                                                            background = key
                                                            showDialogPicker = null
                                                        }
                                                        .padding(
                                                            horizontal = 16.dp,
                                                            vertical = 8.dp
                                                        ),
                                                    color = CustomTheme.colors.d30,
                                                    fontFamily = Jost,
                                                    fontWeight = FontWeight.Medium,
                                                    fontSize = 18.sp,
                                                    lineHeight = 22.sp,
                                                )
                                                HorizontalDivider(color = CustomTheme.colors.l10)
                                            }
                                        }
                                        ActiveField.FieldTwo -> DeckMetaMaps.specialty.forEach { (key, value) ->
                                            item {
                                                Text(
                                                    text = stringResource(value),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable {
                                                            specialty = key
                                                            showDialogPicker = null
                                                        }
                                                        .padding(
                                                            horizontal = 16.dp,
                                                            vertical = 8.dp
                                                        ),
                                                    color = CustomTheme.colors.d30,
                                                    fontFamily = Jost,
                                                    fontWeight = FontWeight.Medium,
                                                    fontSize = 18.sp,
                                                    lineHeight = 22.sp,
                                                )
                                                HorizontalDivider(color = CustomTheme.colors.l10)
                                            }
                                        }
                                        else -> items(
                                            count = roles.itemCount,
                                            key = roles.itemKey(CardListItemProjection::id),
                                            contentType = roles.itemContentType { it }
                                        ) { index ->
                                            val item = roles[index] ?: return@items
                                            Text(
                                                text = item.name.toString(),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        role = item.id to item.name.toString()
                                                        showDialogPicker = null
                                                    }
                                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                                color = CustomTheme.colors.d30,
                                                fontFamily = Jost,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 18.sp,
                                                lineHeight = 22.sp,
                                            )
                                            HorizontalDivider(color = CustomTheme.colors.l10)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    1 -> Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.starter_deck_title),
                            color = CustomTheme.colors.d30,
                            fontFamily = Jost,
                            fontWeight = FontWeight.Medium,
                            fontSize = 20.sp,
                            lineHeight = 22.sp,
                        )
                        StarterDecks.starterDecks.forEachIndexed { index, starterDeck ->
                            val starterRole by decksViewModel.getCard(
                                starterDeck.meta.jsonObject["role"]?.jsonPrimitive?.content.toString()
                            ).collectAsState(null)
                            if (starterRole != null) StarterDeck(
                                onclick = { selectedStarterDeck = index },
                                isSelected = selectedStarterDeck == index,
                                imageSrc = starterRole!!.realImageSrc.toString(),
                                name = starterRole!!.name.toString(),
                                starterDeck = starterDeck,
                                isDarkTheme = isDarkTheme
                            )
                        }
                    }
                }
                val context = LocalContext.current.applicationContext
                if (user.currentUser != null && decksViewModel.isConnected(context)) Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clickable { isUploading = !isUploading },
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.upload_to_rangersdb),
                        color = CustomTheme.colors.d30,
                        fontFamily = Jost,
                        fontWeight = FontWeight.Medium,
                        fontSize = 20.sp,
                        lineHeight = 22.sp,
                        modifier = Modifier.weight(1f)
                    )
                    RadioButton(
                        selected = isUploading,
                        onClick = { isUploading = !isUploading },
                        colors = RadioButtonDefaults.colors().copy(
                            selectedColor = CustomTheme.colors.m,
                            unselectedColor = CustomTheme.colors.m
                        ),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SquareButton(
                    stringId = R.string.cancel_button,
                    leadingIcon = R.drawable.close_32dp,
                    onClick = onCancel,
                    buttonColor = ButtonDefaults.buttonColors()
                        .copy(CustomTheme.colors.warn),
                    iconColor = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                    textColor = if (isDarkTheme) CustomTheme.colors.d30 else CustomTheme.colors.l30,
                    modifier = Modifier.weight(1f)
                )
                val postfix = stringResource(R.string.starter_deck_name_postfix)
                val context = LocalContext.current.applicationContext
                SquareButton(
                    stringId = R.string.create_deck_button,
                    leadingIcon = R.drawable.add_32dp,
                    onClick = {
                        coroutine.launch {
                            isCreating = true
                            decksViewModel.createDeck(
                                name = name,
                                background = background,
                                specialty = specialty,
                                role = role.first,
                                isUploading = isUploading,
                                starterDeckId = selectedStarterDeck,
                                postfix = postfix,
                                user = user,
                                context = context
                            )
                        }.invokeOnCompletion {
                            onCreate.invoke(decksViewModel.deckIdToOpen.value)
                        }
                    },
                    buttonColor = ButtonDefaults.buttonColors().copy(
                        containerColor = CustomTheme.colors.d10,
                        disabledContainerColor = CustomTheme.colors.d10.copy(alpha = 0.25f)
                    ),
                    iconColor = CustomTheme.colors.m,
                    textColor = CustomTheme.colors.l30,
                    modifier = Modifier.weight(1.1f),
                    isEnabled = isLegit
                )
            }
        }
        else Column(
            modifier = modifier
                .background(CustomTheme.colors.l30)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(modifier = Modifier.size(32.dp), color = CustomTheme.colors.m)
        }
    }
}
