# 阅读界面调整

继续使用 Jetpack Compose；未修改数据库、卡片导入格式、TTS、计时器或学习控制逻辑。

## 修改文件

- `app/src/main/java/com/seki999/echowordy/MainActivity.kt`：读取阅读偏好、应用主题及系统栏颜色。
- `app/src/main/java/com/seki999/echowordy/data/local/SettingsDataStore.kt`：持久保存主题和字号。
- `app/src/main/java/com/seki999/echowordy/domain/repository/SettingsRepository.kt`：阅读偏好接口。
- `app/src/main/java/com/seki999/echowordy/ui/review/ReviewScreen.kt`：阅读排版、自动缩小长单词、滚动及响应式控制区。
- `app/src/main/java/com/seki999/echowordy/ui/settings/SettingsScreen.kt`：主题及字号选项。
- `app/src/main/java/com/seki999/echowordy/ui/settings/SettingsViewModel.kt`：读取及保存阅读设置。
- `app/src/main/java/com/seki999/echowordy/ui/theme/Color.kt`：集中定义三套阅读颜色。
- `app/src/main/java/com/seki999/echowordy/ui/theme/Theme.kt`：Material 主题和阅读偏好注入。
- `app/src/main/java/com/seki999/echowordy/ui/theme/Type.kt`：通用字体加大、正文使用 Medium。
- `app/src/main/java/com/seki999/echowordy/ui/edit/PasteCardsScreen.kt`：错误提示跟随主题。
- `app/src/main/res/values/themes.xml`：启动窗口柔和浅色背景。
- `app/src/test/java/com/seki999/echowordy/fakes/FakeSettingsRepository.kt`：支持新增阅读偏好接口。

## 新增文件

- `app/src/main/java/com/seki999/echowordy/domain/model/ReadingPreferences.kt`：主题、字号倍率和默认值。
- `app/src/main/java/com/seki999/echowordy/ui/theme/Dimensions.kt`：阅读字号、行距、边距和区域间距。
- `app/src/main/java/com/seki999/echowordy/ui/review/ReadingBody.kt`：仅显示层的正文识别与例句拆分。
- `app/src/test/java/com/seki999/echowordy/ui/review/ReadingBodyTest.kt`：分段及内容完整性测试。
- `UI_READING.md`：本说明。

## 调整方法

- 默认字号：修改 `ReadingPreferences.fontSize`，当前为 `LARGE`（1.15）；已有用户的保存值优先。
- 各内容基础字号：修改 `ReadingDimensions`；正文、IPA、释义、搭配和例句按倍率变化，标题上限 52sp。
- 默认背景：修改 `Color.kt` 中 `ReadingBackground`（#F6F4EE）；启动窗口颜色在 `themes.xml` 同步修改。
- 默认行距：修改 `ReadingDimensions.ReadingLineSpacing`，当前为 1.5 倍。
- 主题切换：Settings → 阅读主题 → 柔和浅色 / 暖色护眼 / 深色，立即生效并保存。
- 字号切换：Settings → 字体大小 → 小 / 标准 / 大 / 特大，倍率为 0.90 / 1.00 / 1.15 / 1.30。

## 显示策略和验证范围

普通屏幕正文独立滚动，底部控制固定。窄屏、按钮宽度不足（含系统放大字体）或低高度横屏时，按钮改为纵向排列，整页滚动，保证阅读内容与控制均可访问。超长单词优先缩小，达到最小字号后换行，不截断。

原始正文是自由文本：常见 IPA、释义、搭配和例句采用对应样式；中文句末标点后紧跟的英文拆成独立段落。不改写保存的卡片或 TTS 输入。任意格式无法保证精确识别其语义，但内容完整显示。

本机没有连接设备，SDK 中没有 emulator 程序。普通手机、窄屏、系统大字体及三种主题的实际渲染，以及实际设备 TTS 和播放操作，仍需设备验证，不能以单元测试替代。
