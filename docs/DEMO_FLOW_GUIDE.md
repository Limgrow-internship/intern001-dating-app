# Hướng Dẫn Demo Flow Chi Tiết - HeartOn Dating App

## Tổng Quan Ứng Dụng

HeartOn là ứng dụng hẹn hò với các tính năng: Discovery (swipe matching), Chat real-time, Profile management với AI, Premium subscriptions, và Notifications.

## Flow Demo Chi Tiết

### PHẦN 1: ONBOARDING & ĐĂNG KÝ (5-7 phút)

#### 1.1. Màn Hình Splash & Onboarding

- **Splash Screen**: Giới thiệu app với logo
- **Onboarding 3 màn hình**: 
  - "It all starts with a swipe" - Giới thiệu tính năng swipe
  - "Find your vibe, not just a match" - Giới thiệu tìm kiếm theo sở thích
  - Call-to-action để bắt đầu

**Điểm nhấn**: UI/UX hiện đại, animations mượt mà

#### 1.2. Đăng Ký/Đăng Nhập

**3 phương thức đăng nhập:**

1. **Email/Password**: 
   - Nhập email và password
   - Validation real-time
   - Link "Forgot password"

2. **Google Sign-In**:
   - One-tap sign-in
   - Tự động lấy thông tin từ Google account

3. **Facebook Login**:
   - Social login tích hợp
   - Permission request rõ ràng

**Happy Case**: Đăng nhập thành công → Chuyển đến profile setup

#### 1.3. Profile Setup (5 bước - Quan trọng nhất)

**Step 1: Upload Photos (0%)**
- Upload tối thiểu 1 ảnh (tối đa 3 ảnh)
- Image picker với preview
- Có thể chọn từ gallery hoặc camera

**Step 2: Tên (10%)**
- Nhập tên hiển thị
- Validation không để trống

**Step 3: Giới tính (20%)**
- Chọn: Male / Female / Other
- UI selector đẹp mắt

**Step 4: Ngày sinh (30%)**
- Date picker
- Tính toán tuổi tự động
- Validation tuổi hợp lệ

**Step 5: Relationship Mode (40%)**
- Dating Mode (hẹn hò)
- Friend Mode (kết bạn)
- Single selection

**Kết thúc**: Profile setup thành công → Navigate to Home

### PHẦN 2: DISCOVERY & MATCHING (10-15 phút)

#### 2.1. Màn Hình Home - Tab "For You"

**Tính năng:**
- **Location-based Discovery**: App yêu cầu location permission
- **Discovery Cards**: Hiển thị các profile gần bạn
- **Swipe Gestures**:
  - Swipe RIGHT → Like (màu vàng gradient overlay)
  - Swipe LEFT → Pass (màu xám overlay)
  - Swipe UP → Super Like (special animation)
  - Tap để xem profile chi tiết

**Điểm nhấn demo:**
- Swipe một vài cards
- Xem profile detail (photos, bio, interests, age, location)
- Demo Super Like với animation đẹp

#### 2.2. Match Success (Happy Case #1)

**Kịch bản:**
1. User A like User B
2. User B đã like User A trước đó
3. **Match Overlay Dialog** xuất hiện ngay lập tức
4. Hiển thị:
   - Ảnh của người match
   - Animation "It's a Match!"
   - Button "Send Message" → Navigate to Chat
   - Button "Keep Swiping" → Tiếp tục discovery

**Điểm nhấn**: Animation mượt mà, cảm xúc tích cực

#### 2.3. Tab "Liked You"

- Hiển thị danh sách người đã like profile của bạn
- Grid layout với avatar
- Click vào user → Navigate to Dating Mode để xem chi tiết và quyết định like/pass

**Happy Case**: Có nhiều likes → Tạo cảm giác hấp dẫn

### PHẦN 3: CHAT & MESSAGING (10 phút)

#### 3.1. Chat List Screen

- Danh sách các matches
- Hiển thị: Avatar, Name, Last message preview, Timestamp
- Unread message badge
- Sắp xếp theo activity

#### 3.2. Chat Detail - Real-time Messaging

**Tính năng Chat:**

1. **Text Messages**:
   - Send/receive real-time qua WebSocket
   - Message status (sent, delivered)
   - Timestamp formatting
   - Message bubbles (left/right)

2. **Voice Messages** (Happy Case #2):
   - Hold-to-record button
   - Record audio với permission request
   - Auto-send khi thả tay
   - Audio player với waveform animation
   - Duration hiển thị
   - Play/pause control

3. **Image Messages**:
   - Chọn ảnh từ gallery
   - Upload và gửi trong chat
   - Image preview trong chat

4. **Reply to Messages**:
   - Long press message → Show reply option
   - Reply bar hiển thị message được reply
   - Thread-like conversation

5. **Message Reactions**:
   - Long press → Show emoji reactions
   - React với emoji (like, love, laugh, etc.)

6. **Suggestion Chips**:
   - AI-powered conversation starters
   - Quick reply suggestions
   - Ví dụ: "Xin chào! Bạn có khỏe không?"

**Điểm nhấn demo:**
- Demo voice message (hold, record, send)
- Demo real-time receive message (mở 2 thiết bị)
- Demo reply và reactions

### PHẦN 4: PROFILE MANAGEMENT (5 phút)

#### 4.1. Edit Profile

**Các thông tin có thể chỉnh sửa:**
- Basic info: Name, Age, Gender, Location
- Photos: Add/Remove/Reorder
- Bio: Text editor
- Interests: Multi-select tags
- Goals: Relationship goals
- Job, Education, Height, Weight
- Zodiac sign
- Open questions: Custom Q&A

#### 4.2. AI Bio Generator (Happy Case #3)

**Tính năng:**
- Icon "Magic Pen" bên cạnh bio field
- Click → Dialog nhập prompt
- Ví dụ: "Tôi thích du lịch, đọc sách và nấu ăn"
- AI generate bio dựa trên prompt
- Loading animation với messages:
  - "Đang phân tích ý tưởng của bạn..."
  - "AI đang tạo bio phù hợp..."
  - "Đang tinh chỉnh nội dung..."
- Kết quả: Bio được generate tự động
- Có thể edit sau khi generate

**Điểm nhấn**: Tính năng AI độc đáo, tiết kiệm thời gian

#### 4.3. Verify Account

- Upload verification photo
- Chờ admin approve
- Verified badge xuất hiện sau khi approved

### PHẦN 5: NOTIFICATIONS (3 phút)

#### 5.1. Notification Types

1. **New Match**: Khi có match mới
2. **New Message**: Khi nhận tin nhắn mới
3. **New Like**: Khi có người like bạn
4. **Super Like**: Khi có người super like bạn
5. **Verification**: Khi account được verify

#### 5.2. Notification Settings

- Master toggle: Bật/tắt tất cả notifications
- Individual toggles cho từng loại
- Deep linking: Click notification → Navigate đến đúng screen

**Happy Case**: Demo push notification khi có match/message mới

### PHẦN 6: PREMIUM FEATURES (5 phút)

#### 6.1. Premium Tiers

**3 gói subscription:**

1. **BASIC** (Free):
   - Match ✓
   - Send message ✓
   - Video & voice ✗
   - Unlimited Like ✗
   - Rewind ✗
   - Super Like ✗
   - Hide Ads ✗

2. **GOLD**:
   - Tất cả Basic features
   - Video & voice ✗
   - Unlimited Like ✗
   - Rewind ✗
   - Super Like ✗
   - Hide Ads ✗
   - View list ✗

3. **ELITE**:
   - Tất cả Gold features
   - View list ✓
   - Send before match ✓ (nếu có)

**Pricing:**
- Weekly: 20,000 VND
- Monthly: 59,000 VND (Save 21,000 VND)

#### 6.2. Premium Benefits Demo

- **No Ads**: Premium users không thấy ads
- **Unlimited Likes**: Không giới hạn số lượt like
- **Super Like**: Highlight profile của bạn
- **Rewind**: Undo last swipe action

**Điểm nhấn**: Giá cả hợp lý, features rõ ràng

### PHẦN 7: SETTINGS & MORE (3 phút)

#### 7.1. Settings Menu

- Change Password
- Notification Settings
- Language Selection
- Logout
- Delete Account

#### 7.2. Location Services

- Permission request khi vào app
- Auto-update location
- Location-based discovery

## Happy Cases Nổi Bật

### 🎯 Happy Case #1: Match Success

**Mô tả**: Khi 2 người like nhau, màn hình match overlay xuất hiện với animation đẹp mắt

**Flow:**
1. User A swipe right (like) User B
2. User B đã like User A trước đó
3. Match dialog hiển thị ngay
4. Option "Send Message" → Chat ngay lập tức

**Tại sao nổi bật**: 
- Instant gratification
- Visual feedback rõ ràng
- Seamless transition to chat

### 🎯 Happy Case #2: Voice Messages

**Mô tả**: Tính năng gửi tin nhắn giọng nói với UX tuyệt vời

**Flow:**
1. Long press voice button
2. Record audio (waveform animation)
3. Release → Auto send
4. Receiver có thể play ngay

**Tại sao nổi bật**:
- Intuitive UX (hold-to-record)
- Personal connection
- Faster than typing
- Modern dating app standard

### 🎯 Happy Case #3: AI Bio Generator

**Mô tả**: AI hỗ trợ tạo bio dựa trên prompt của user

**Flow:**
1. Click magic pen icon
2. Nhập prompt về sở thích/tính cách
3. AI generate bio
4. User có thể edit hoặc save

**Tại sao nổi bật**:
- Innovation, AI-powered
- Giải quyết pain point (khó viết bio)
- Time-saving
- Unique feature so với competitors

### 🎯 Happy Case #4: Liked You Feature

**Mô tả**: Xem ai đã like profile của mình

**Flow:**
1. Tab "Liked You" hiển thị danh sách
2. Grid view với avatars
3. Click vào user → Xem profile và quyết định

**Tại sao nổi bật**:
- Tăng engagement
- Mutual attraction insight
- Premium feel (giống Tinder Gold)

### 🎯 Happy Case #5: Real-time Chat với WebSocket

**Mô tả**: Chat real-time, instant message delivery

**Flow:**
1. Send message → Instant delivery
2. Typing indicators
3. Message status
4. Socket reconnection tự động

**Tại sao nổi bật**:
- Smooth real-time experience
- No delays
- Modern architecture

### 🎯 Happy Case #6: Location-based Discovery

**Mô tả**: Tìm người gần bạn dựa trên GPS

**Flow:**
1. Permission request location
2. Auto-sync location
3. Discovery cards hiển thị distance
4. Filter by distance range

**Tại sao nổi bật**:
- Practical utility
- Local connections
- Safety (gần nhà)

### 🎯 Happy Case #7: Notification Deep Linking

**Mô tả**: Push notification → Navigate đúng màn hình

**Flow:**
1. Nhận notification "New Match"
2. Click → Navigate to Chat với match đó
3. Seamless experience

**Tại sao nổi bật**:
- Convenience
- Engagement boost
- Professional implementation

## Tips Demo Cho Khách Hàng

### Thứ Tự Demo Đề Xuất:

1. **Onboarding** (5 phút) - First impression
2. **Profile Setup** (5 phút) - Onboarding experience
3. **Discovery & Match** (10 phút) - Core feature, highlight Match success
4. **Chat với Voice** (8 phút) - Highlight voice messages
5. **AI Bio** (3 phút) - Unique feature
6. **Liked You** (2 phút) - Engagement feature
7. **Premium** (5 phút) - Monetization
8. **Q&A** (5 phút)

### Điểm Nhấn Khi Demo:

- ✅ **Speed**: App nhanh, không lag
- ✅ **UX**: Intuitive, dễ dùng
- ✅ **Features**: Đầy đủ tính năng hiện đại
- ✅ **AI**: Innovation với AI bio
- ✅ **Real-time**: Chat instant
- ✅ **Monetization**: Premium tiers rõ ràng

### Cần Chuẩn Bị:

- 2+ thiết bị để demo real-time chat/match
- Test accounts sẵn có
- Internet connection ổn định
- Location enabled
- Camera/Microphone permissions ready

## Kết Luận

App có đầy đủ tính năng của một dating app hiện đại với các điểm nổi bật:

- **AI-powered bio generation** (unique)
- **Real-time chat với voice messages**
- **Smooth swipe experience**
- **Premium monetization strategy**
- **Location-based discovery**
- **Professional notifications**

Flow demo đề xuất: **45-60 phút** để cover tất cả features quan trọng.

