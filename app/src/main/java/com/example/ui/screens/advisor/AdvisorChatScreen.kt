package com.example.ui.screens.advisor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.ChatMessage
import com.example.data.repository.ChatSession
import com.example.domain.model.BusinessType
import com.example.ui.components.MasarBottomNavigation
import com.example.ui.components.MasarHeader
import com.example.ui.components.MasarRtlProvider
import com.example.ui.theme.MasarEmerald
import com.example.ui.theme.MasarGold
import com.example.ui.theme.MasarRose
import com.example.ui.theme.MasarSky
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MasarUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

/**
 * Modern ChatGPT / Gemini styled "اسأل مسار" Advisor Screen.
 * - Single message transmission on click (Debounced against duplicate clicks)
 * - Natural scrolling that doesn't force user down while reading
 * - Complete Chat History & Sessions Management (Drawer/Sheet)
 * - New Chat & Clear Chat features with confirmation
 * - Context-aware smart prompts & simulation action buttons
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AdvisorChatScreen(
    state: MasarUiState,
    onNavigate: (AppScreen) -> Unit,
    onSendMessage: (String) -> Unit,
    onStartNewChat: () -> Unit,
    onSwitchChatSession: (String) -> Unit,
    onDeleteChatSession: (String) -> Unit,
    onClearChat: () -> Unit
) {
    MasarRtlProvider {
        var inputMessage by remember { mutableStateOf("") }
        var showHistorySheet by remember { mutableStateOf(false) }
        var showClearAllDialog by remember { mutableStateOf(false) }
        var sessionToDelete by remember { mutableStateOf<ChatSession?>(null) }
        
        val listState = rememberLazyListState()
        val coroutineScope = rememberCoroutineScope()
        val isKeyboardOpen = WindowInsets.isImeVisible
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        // Filter messages for current conversation
        val currentMessages = remember(state.chatMessages, state.currentConversationId) {
            val filtered = state.chatMessages.filter { it.conversationId == state.currentConversationId }
            // Fallback: If current conversation has no filtered messages and is default_session, show all without convId or matching default
            if (filtered.isEmpty() && state.currentConversationId == "default_session" && state.chatMessages.isNotEmpty()) {
                state.chatMessages.filter { it.conversationId == "default_session" || it.conversationId.isBlank() }
            } else {
                filtered
            }
        }

        // Keep track of previous message count to only auto-scroll on NEW messages
        var previousMessageCount by remember { mutableIntStateOf(currentMessages.size) }

        // Proactive suggestions based on exact live financial conditions
        val proactiveQuestions = remember(state.profile, state.metrics) {
            val list = mutableListOf<String>()

            // Case 1: Low runway
            if (state.metrics.runwayMonths < 6 && state.metrics.burnRate > 0) {
                list.add("لاحظت أن فترة الأمان (Runway) انخفضت، كيف أرشد مصاريفي فوراً؟")
            }

            // Case 2: Frozen assets
            if (state.profile.hasFrozenAssets && state.profile.frozenAssetsValue > 0) {
                list.add("لدي أصول غير مستغلة، ما هي أفضل خطة لتسييلها أو الاستثمار بها؟")
            }

            // Case 3: Employee vs Business specific prompts
            if (state.profile.businessType == BusinessType.EMPLOYEE) {
                list.add("كيف أوزع راتبي الشهري بذكاء (50/30/20) لبناء صندوق طوارئ؟")
                list.add("هل وضعي المالي والسيولة تسمح بشراء أصل كبير الآن؟")
                list.add("كيف أستثمر الفائض من دخلي وأقلل الهدر المعيشي؟")
            } else {
                list.add("هل وضعي المالي آمن حالياً لتوظيف شخص أو التوسع في النشاط؟")
                list.add("كيف أرفع فترة الأمان المالي (Runway) لأكثر من 12 شهراً؟")
                list.add("كيف أرشد تكاليف التشغيل بنسبة 20% دون التأثير على نمو العمل؟")
            }

            list
        }

        // Auto-scroll ONLY when a brand new message arrives, allowing free upward scrolling
        LaunchedEffect(currentMessages.size) {
            if (currentMessages.size > previousMessageCount && currentMessages.isNotEmpty()) {
                listState.animateScrollToItem(currentMessages.size - 1)
            }
            previousMessageCount = currentMessages.size
        }

        // Delete Single Session Confirmation Dialog
        sessionToDelete?.let { session ->
            AlertDialog(
                onDismissRequest = { sessionToDelete = null },
                title = { Text("حذف المحادثة", fontWeight = FontWeight.Bold) },
                text = { Text("هل أنت متأكد من حذف محادثة: \"${session.title}\"؟") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onDeleteChatSession(session.id)
                            sessionToDelete = null
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MasarRose)
                    ) {
                        Text("حذف", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { sessionToDelete = null }) {
                        Text("إلغاء")
                    }
                }
            )
        }

        // Clear All History Confirmation Dialog
        if (showClearAllDialog) {
            AlertDialog(
                onDismissRequest = { showClearAllDialog = false },
                title = { Text("مسح كافة سجلات المحادثات", fontWeight = FontWeight.Bold) },
                text = { Text("سيتم مسح جميع المحادثات السابقة والاستشارات المسجلة نهائياً.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showClearAllDialog = false
                            onClearChat()
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MasarRose)
                    ) {
                        Text("مسح الكل", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearAllDialog = false }) {
                        Text("إلغاء")
                    }
                }
            )
        }

        // Chat History Modal Bottom Sheet
        if (showHistorySheet) {
            ModalBottomSheet(
                onDismissRequest = { showHistorySheet = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface,
                dragHandle = null
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    // Header of the BottomSheet
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = MasarEmerald,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "سجل المحادثات السابقة",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(onClick = { showHistorySheet = false }) {
                            Icon(Icons.Default.Close, contentDescription = "إغلاق")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Button to start a brand new conversation from inside sheet
                    Button(
                        onClick = {
                            showHistorySheet = false
                            onStartNewChat()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MasarEmerald)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("بدء محادثة جديدة", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    Spacer(modifier = Modifier.height(12.dp))

                    // Sessions List
                    if (state.chatSessions.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 36.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChatBubbleOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                    modifier = Modifier.size(40.dp)
                                )
                                Text(
                                    text = "لا توجد محادثات سابقة حتى الآن",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(320.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(state.chatSessions, key = { it.id }) { session ->
                                val isActive = session.id == state.currentConversationId
                                val dateStr = remember(session.timestamp) {
                                    val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale("ar"))
                                    sdf.format(Date(session.timestamp))
                                }

                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isActive) MasarEmerald.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    border = BorderStroke(
                                        width = if (isActive) 1.5.dp else 1.dp,
                                        color = if (isActive) MasarEmerald else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onSwitchChatSession(session.id)
                                            showHistorySheet = false
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isActive) MasarEmerald else MaterialTheme.colorScheme.surfaceVariant),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ChatBubbleOutline,
                                                    contentDescription = null,
                                                    tint = if (isActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }

                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(
                                                        text = session.title,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    if (isActive) {
                                                        Surface(
                                                            shape = RoundedCornerShape(6.dp),
                                                            color = MasarEmerald.copy(alpha = 0.2f)
                                                        ) {
                                                            Text(
                                                                text = "الحالية",
                                                                style = MaterialTheme.typography.labelSmall,
                                                                color = MasarEmerald,
                                                                fontSize = 10.sp,
                                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                            )
                                                        }
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(2.dp))

                                                Text(
                                                    text = "$dateStr • ${session.messageCount} رسائل",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }

                                        IconButton(
                                            onClick = { sessionToDelete = session },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DeleteOutline,
                                                contentDescription = "حذف المحادثة",
                                                tint = MasarRose.copy(alpha = 0.7f),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (state.chatSessions.isNotEmpty()) {
                        TextButton(
                            onClick = { showClearAllDialog = true },
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            colors = ButtonDefaults.textButtonColors(contentColor = MasarRose)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("مسح كافة سجلات المحادثات", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                MasarHeader(
                    title = "اسأل مسار (المرشد الذكي)",
                    subtitle = "مرشد استباقي يحلل وضعك المالي ويقترح الحلول",
                    currentScreen = AppScreen.ADVISOR,
                    onNavigate = onNavigate
                )
            },
            bottomBar = {
                if (!isKeyboardOpen) {
                    MasarBottomNavigation(
                        currentScreen = AppScreen.ADVISOR,
                        onNavigate = onNavigate
                    )
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = paddingValues.calculateTopPadding(),
                        bottom = if (!isKeyboardOpen) paddingValues.calculateBottomPadding() else 0.dp
                    )
                    .imePadding()
            ) {
                // Top Action Toolbar (History & New Chat)
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // History Button with Badge
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                            modifier = Modifier.clickable { showHistorySheet = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = "سجل الدردشات",
                                    tint = MasarEmerald,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "سجل الدردشات",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (state.chatSessions.isNotEmpty()) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MasarEmerald
                                    ) {
                                        Text(
                                            text = "${state.chatSessions.size}",
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Action: Start New Chat (+)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MasarEmerald.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, MasarEmerald.copy(alpha = 0.3f)),
                            modifier = Modifier.clickable { onStartNewChat() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = MasarEmerald,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "محادثة جديدة",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MasarEmerald
                                )
                            }
                        }
                    }
                }

                // Chat Messages List
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Proactive Context Welcome Banner
                    if (currentMessages.isEmpty()) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(MasarEmerald.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.Psychology,
                                                contentDescription = null,
                                                tint = MasarEmerald,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        Column {
                                            Text(
                                                "مرحباً بك! أنا مرشد مسار المالي",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                "مستشارك الذكي المتصل بأرقامك لحظياً",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Proactive observation pills
                                    if (state.metrics.runwayMonths < 6 && state.metrics.burnRate > 0) {
                                        ObservationCard(
                                            icon = Icons.Default.HourglassBottom,
                                            title = "تنبيه انخفاض فترة الأمان:",
                                            body = "لاحظت أن السيولة تكفي لـ ${String.format("%.1f", state.metrics.runwayMonths)} شهراً. هل ترغب في معرفة خطة ترشيد فورية؟",
                                            tint = MasarRose,
                                            enabled = !state.isChatLoading,
                                            onClick = {
                                                if (!state.isChatLoading) {
                                                    onSendMessage("لاحظت أن فترة الأمان منخفضة، ما هي خطة الترشيد المقترحة؟")
                                                }
                                            }
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }

                                    if (state.profile.hasFrozenAssets && state.profile.frozenAssetsValue > 0) {
                                        ObservationCard(
                                            icon = Icons.Default.MonetizationOn,
                                            title = "فرصة استغلال الأصول:",
                                            body = "لديك أصول مجمدة بقيمة ${String.format("%,.0f", state.profile.frozenAssetsValue)} ${state.profile.baseCurrency}. هل تريد معرفة أفضل طرق تسييلها أو استثمارها؟",
                                            tint = MasarGold,
                                            enabled = !state.isChatLoading,
                                            onClick = {
                                                if (!state.isChatLoading) {
                                                    onSendMessage("لدي أصول مجمدة، ما هي أفضل خطة لتسييلها أو استثمارها؟")
                                                }
                                            }
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }

                                    Text(
                                        "اسألني عن أي قرار مالي، جدوى شراء، توزيع راتب، أو محاكاة استثمار، وسأقدم لك إجابات دقيقة مبنية على واقعك المالي.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 20.sp
                                    )
                                }
                            }
                        }
                    }

                    items(currentMessages, key = { it.id }) { msg ->
                        ChatBubble(
                            message = msg,
                            onNavigateToSimulator = { onNavigate(AppScreen.SIMULATOR) }
                        )
                    }

                    if (state.isChatLoading) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = MasarEmerald
                                    )
                                    Text(
                                        "جاري التحليل المالي وصياغة التوصية...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Proactive Suggestion Chips Bar
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "أسئلة مقترحة حسب وضعك المالي:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                    )
                    LazyRow(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(proactiveQuestions) { prompt ->
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier.clickable(enabled = !state.isChatLoading) {
                                    if (!state.isChatLoading) {
                                        onSendMessage(prompt)
                                    }
                                }
                            ) {
                                Text(
                                    text = prompt,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (state.isChatLoading) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }

                // Chat Input Field
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = inputMessage,
                            onValueChange = { inputMessage = it },
                            placeholder = {
                                Text(
                                    "اكتب سؤالك أو استشارتك المالية هنا...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MasarEmerald,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            ),
                            maxLines = 4
                        )

                        IconButton(
                            onClick = {
                                if (inputMessage.isNotBlank() && !state.isChatLoading) {
                                    val text = inputMessage
                                    inputMessage = ""
                                    onSendMessage(text)
                                }
                            },
                            enabled = inputMessage.isNotBlank() && !state.isChatLoading,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(if (inputMessage.isNotBlank() && !state.isChatLoading) MasarEmerald else MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "إرسال",
                                tint = if (inputMessage.isNotBlank() && !state.isChatLoading) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ObservationCard(
    icon: ImageVector,
    title: String,
    body: String,
    tint: Color,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = tint.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, tint.copy(alpha = 0.3f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = tint)
                Text(text = body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
private fun ChatBubble(
    message: ChatMessage,
    onNavigateToSimulator: () -> Unit
) {
    val isUser = message.sender.equals("user", ignoreCase = true)
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val isSimulationAdvice = !isUser && (message.content.contains("محاكاة") || message.content.contains("المحاكي") || message.content.contains("شراء") || message.content.contains("تقسيط"))

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            color = if (isUser) MasarEmerald else MaterialTheme.colorScheme.surfaceVariant,
            border = if (!isUser) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)) else null,
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                    lineHeight = 22.sp
                )

                // Quick Action to simulate this decision in the simulator
                if (isSimulationAdvice) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MasarEmerald.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, MasarEmerald.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToSimulator() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MasarEmerald,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "فتح المحاكي لتجربة هذا القرار ⚡",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MasarEmerald
                            )
                        }
                    }
                }
            }
        }
    }
}
