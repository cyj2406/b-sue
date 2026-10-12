package com.bsue.blob.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bsue.blob.ui.theme.BsueShape
import com.bsue.blob.ui.theme.BsueSpace
import com.bsue.blob.ui.theme.BsueType
import com.bsue.blob.ui.theme.Role

// ════════════════════════════════════════════════
//  카드 · 리스트 행 · 진행률 · 칩 · 배지 — DESIGN.md 5번
// ════════════════════════════════════════════════

// 카드: 흰 배경 · 모서리 20 · 안쪽 24(위아래 20) · 그림자 없음
// title 을 주면 맨 위에 카드 제목(title3) + 오른쪽 작은 링크("전체보기 ›")
@Composable
fun BsueCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    link: String? = null,
    onLink: () -> Unit = {},
    contentPadding: Boolean = true, // 리스트 행을 넣을 땐 false (행이 자기 여백을 가짐)
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier
            .fillMaxWidth()
            .clip(BsueShape.Card)
            .background(Role.Surface)
            .padding(vertical = BsueSpace.CardPaddingV),
    ) {
        if (title != null) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = BsueSpace.CardPaddingH),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(title, style = BsueType.Title3, color = Role.TextPrimary, modifier = Modifier.weight(1f))
                if (link != null) {
                    Text(
                        link,
                        style = BsueType.Caption,
                        color = Role.TextSecondary,
                        modifier = Modifier.clickable(onClick = onLink),
                    )
                }
            }
            Spacer(Modifier.height(BsueSpace.SectionGap))
        }
        Column(if (contentPadding) Modifier.padding(horizontal = BsueSpace.CardPaddingH) else Modifier) {
            content()
        }
    }
}

// 아바타: 연한 원 안에 블롭 미션 포즈 썸네일 (40 / 56)
@Composable
fun Avatar(@DrawableRes image: Int, size: Dp = 40.dp, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(size)
            .clip(CircleShape)
            .background(Role.AvatarBg),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Image(
            painter = painterResource(image),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            alignment = Alignment.BottomCenter,
            modifier = Modifier.size(size * 0.86f),
        )
    }
}

// 리스트 행
// [아바타]  제목 (body1)              값 (body1 Bold) ›
//           보조 (caption, 회색)       보조값 (caption)
// valueColor: 지킨 돈은 Role.Point, 쓴 돈은 Role.TextPrimary 또는 Role.TextSecondary (빨강 없음)
@Composable
fun ListRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    value: String? = null,
    subValue: String? = null,
    valueColor: Color = Role.TextPrimary,
    titleColor: Color = Role.TextPrimary,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null, // 값 대신 배지·스위치 등
    chevron: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = if (subtitle != null) BsueSpace.ListRowTwoLine else BsueSpace.ListRow)
            .background(if (pressed) Role.Pressed else Color.Transparent)
            .then(
                if (onClick != null) Modifier.clickable(interactionSource = interaction, indication = null, onClick = onClick)
                else Modifier,
            )
            .padding(horizontal = BsueSpace.ScreenSide, vertical = BsueSpace.X12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leading != null) {
            leading()
            Spacer(Modifier.width(BsueSpace.IconTextGap))
        }
        Column(Modifier.weight(1f)) {
            Text(title, style = BsueType.Body1, color = titleColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (subtitle != null) {
                Text(subtitle, style = BsueType.Caption, color = Role.TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        if (value != null || subValue != null) {
            Spacer(Modifier.width(BsueSpace.X12))
            Column(horizontalAlignment = Alignment.End) {
                if (value != null) {
                    Text(value, style = BsueType.Body1.copy(fontWeight = FontWeight.Bold), color = valueColor)
                }
                if (subValue != null) {
                    Text(subValue, style = BsueType.Caption, color = Role.TextSecondary)
                }
            }
        }
        if (trailing != null) {
            Spacer(Modifier.width(BsueSpace.X12))
            trailing()
        }
        if (chevron) {
            Spacer(Modifier.width(BsueSpace.X8))
            Text("›", style = BsueType.Body1, color = Role.TextDisabled)
        }
    }
}

// 진행률: 높이 8 · 둥근 끝 · 연한 트랙 + 포인트색 채움. label 을 주면 위에 작게 ("12명 중 5명 합류")
@Composable
fun BsueProgress(fraction: Float, modifier: Modifier = Modifier, label: String? = null) {
    Column(modifier.fillMaxWidth()) {
        if (label != null) {
            Text(label, style = BsueType.Caption, color = Role.TextSecondary)
            Spacer(Modifier.height(BsueSpace.X8))
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(BsueShape.Pill)
                .background(Role.PointTrack),
        ) {
            Box(
                Modifier
                    .fillMaxWidth(fraction.coerceIn(0f, 1f))
                    .height(8.dp)
                    .clip(BsueShape.Pill)
                    .background(Role.Point),
            )
        }
    }
}

// 칩: 높이 32 · 회색 / 선택하면 연한 포인트색
@Composable
fun BsueChip(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .height(32.dp)
            .clip(BsueShape.Pill)
            .background(if (selected) Role.PointWeakBg else Role.SecondaryBg)
            .clickable(onClick = onClick)
            .padding(horizontal = BsueSpace.X12),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            style = BsueType.Body2.copy(fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal),
            color = if (selected) Role.Point else Role.SecondaryText,
        )
    }
}

// 칩 여러 개를 한 줄로
@Composable
fun BsueChipRow(options: List<String>, selected: String, modifier: Modifier = Modifier, onSelect: (String) -> Unit) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(BsueSpace.X8)) {
        options.forEach { option -> BsueChip(option, option == selected) { onSelect(option) } }
    }
}

// 배지: "D-1", "NEW" — 높이 20 · small · 포인트색 바탕 흰 글씨
// weak = true 면 연한 바탕 + 포인트색 글씨 (수호신 이름 배지 등)
@Composable
fun BsueBadge(label: String, modifier: Modifier = Modifier, weak: Boolean = false) {
    Box(
        modifier
            .height(20.dp)
            .clip(BsueShape.Pill)
            .background(if (weak) Role.PointWeakBg else Role.Point)
            .padding(horizontal = BsueSpace.X8),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = BsueType.Small, color = if (weak) Role.Point else Color.White)
    }
}

// 카드 안 구분선
@Composable
fun BsueDivider(modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .padding(horizontal = BsueSpace.ScreenSide)
            .height(1.dp)
            .background(Role.Divider),
    )
}
