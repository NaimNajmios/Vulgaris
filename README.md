# Football Agent AI

> **Conversational AI assistant that intelligently orchestrates API-Football data using Cerebras, Groq, Gemini, and OpenRouter**

An agentic mobile application that transforms complex football queries into intelligent, multi-step API orchestrations, delivering natural language insights with a premium Jetpack Compose experience.

---

## ✨ What Makes This Special

### 🤖 Agentic AI System
- **Intelligent Planning**: AI analyzes your query and creates an optimal execution plan
- **Multi-Tool Orchestration**: Chains multiple API calls to answer complex questions
- **Context Awareness**: Remembers conversation history and understands follow-ups
- **Smart Caching**: 70%+ cache hit rate reduces API costs and improves speed

**Example Query:**
```
You: "How has Arsenal performed against top 6 teams this season?"

Agent Process:
1. Searches for Arsenal team ID
2. Fetches all fixtures for current season
3. Filters matches vs top 6 opponents
4. Retrieves detailed stats for those matches
5. Synthesizes natural language response

Response: "Arsenal has struggled against top 6 opponents this season, 
with only 2 wins in 8 matches (25% win rate)..."
```

### 🎨 Modern Design
- Sharp corners and bold accents inspired by modern editorial design
- Spring physics animations for natural interactions
- Haptic feedback for key interactions

### ⚡ Ultra-Fast AI Providers
| Provider | Speed | Use Case |
|----------|-------|----------|
| **Cerebras** | 2000+ tokens/sec | Primary - Ultra-fast planning |
| **Groq** | 800+ tokens/sec | Fallback - Fast inference |
| **Gemini** | 400+ tokens/sec | Fallback - Reliable & multimodal |
| **OpenRouter** | Varies | Last resort - Model variety |

**Smart Fallback**: Automatically switches providers on rate limits or errors

---

## 🚀 Features

### Conversational AI
- ✅ Natural language queries ("When does Liverpool play next?")
- ✅ Complex analysis ("Compare Haaland's home vs away performance")
- ✅ Follow-up questions ("What about their away record?")
- ✅ Multi-turn conversations with context retention
- ✅ Proactive suggestions for related queries

### Football Data Coverage
- ⚽ **Live Scores**: Real-time match updates with 30-second refresh
- 📊 **Player Statistics**: Goals, assists, ratings, detailed performance
- 🏆 **League Standings**: Live tables for all major leagues
- 📈 **Team Form Analysis**: Recent performance trends and patterns
- 🆚 **Head-to-Head**: Historical matchups between teams
- 🏥 **Team News**: Injury lists and squad updates
- 📋 **Match Lineups**: Formations and tactical setups
- 🎲 **Predictions**: AI-powered match outcome analysis

### Premium UX
- 🎭 **Typewriter Effect**: Character-by-character response animation
- 👆 **Swipe Gestures**:
    - Swipe left on match cards → Detailed stats
    - Swipe right on match cards → Add to watchlist
    - Long-press teams → Set as favorite
- 📱 **Bottom Sheet Share**: Share team names from browser → Quick actions
- 💊 **Refinement Pills**: "More Detail", "Simplify", "Compare", or create custom
- 📊 **Live Animations**: Smooth score updates with spring physics

### Analytics & Monitoring
- 📈 **Token Usage Chart**: Track AI costs over 7/30/90 days
- 🎯 **Success Rate Donut**: Per-provider reliability breakdown
- ⏱️ **Response Time Bar Chart**: Performance monitoring
- 🔧 **Tools Usage Stats**: Which endpoints are called most
- 💾 **Cache Hit Rate**: Storage efficiency metrics
- 📝 **Query History**: Last 20 requests with full details

---

## 📋 Requirements

- **Android**: 7.0 (API 24) or higher
- **Storage**: ~50MB for app + cache
- **Internet**: Required for API calls
- **API Keys**: At least one AI provider:
    - [Google Gemini](https://ai.google.dev) (Recommended)
    - [Cerebras](https://cerebras.ai) (Fastest)
    - [Groq](https://console.groq.com) (Fast)
    - [OpenRouter](https://openrouter.ai) (Fallback)
- **API-Football**: [Get free key](https://www.api-football.com/) (100 calls/day)

---

## 🛠️ Installation

### Option 1: Install APK (Easiest)

1. Download latest APK from [Releases](https://github.com/yourusername/football-agent/releases)
2. Enable "Install from Unknown Sources" in Settings
3. Install and launch

### Option 2: Build from Source

```bash
# Clone repository
git clone https://github.com/yourusername/football-agent.git
cd football-agent

# Open in Android Studio
# File → Open → Select project folder

# Build and run
# Run → Run 'app' (Shift + F10)
```

**Requirements for building:**
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17 or higher
- Gradle 8.2+

---

## ⚙️ Setup Guide

### 1. First Launch

On first launch, the app will prompt you to configure API keys.

### 2. AI Provider Setup

1. Tap **Settings** (⚙️) in the top bar
2. Select **AI Provider**:
    - **Cerebras** (recommended for speed)
    - **Groq** (good balance)
    - **Gemini** (most reliable)
    - **OpenRouter** (backup)
3. Enter your API key
4. Select **AI Model** from dropdown
5. Tap **Test Connection** to verify

**Pro Tip**: Configure multiple providers for automatic fallback!

### 3. API-Football Setup

1. Get free API key from [API-Football](https://www.api-football.com/)
2. In Settings → **Data Provider**
3. Paste your API-Football key
4. Tap **Test Connection**

### 4. Customize Preferences

**Appearance**:
- Adjust text size for responses

**Behavior**:
- Enable/disable typewriter animation
- Set default refinement pills
- Configure haptic feedback strength

**Favorites**:
- Add your favorite teams for quick access
- Set preferred leagues for default filters

---

## 💡 Usage Examples

### Simple Queries

```
"When does Liverpool play next?"
→ Agent searches for Liverpool's next fixture

"Show me Premier League table"
→ Agent fetches current standings

"Who scored in the Manchester derby?"
→ Agent retrieves match details and goal scorers
```

### Medium Complexity

```
"Compare Haaland and Mbappé this season"
→ Agent:
  1. Searches for both players
  2. Fetches 2024 season stats for each
  3. Compares goals, assists, minutes played
  4. Presents side-by-side analysis

"How has Arsenal performed away from home?"
→ Agent:
  1. Searches for Arsenal
  2. Fetches all fixtures
  3. Filters away matches
  4. Analyzes win/loss record and stats
```

### Complex Queries

```
"Analyze Manchester United's defensive vulnerabilities 
in their last 5 matches against top 6 teams"

→ Agent:
  1. Searches for Manchester United
  2. Fetches all fixtures
  3. Identifies top 6 opponents
  4. Filters last 5 matches vs top 6
  5. Retrieves detailed match statistics
  6. Analyzes defensive metrics (xG conceded, shots, etc.)
  7. Synthesizes comprehensive report

"Predict the outcome of the Manchester derby based on 
recent form, head-to-head, and injury news"

→ Agent:
  1. Searches for both Manchester teams
  2. Fetches head-to-head history
  3. Analyzes recent form (last 5 matches each)
  4. Retrieves current injury lists
  5. Combines all factors for AI prediction
  6. Provides detailed reasoning
```

### Follow-up Conversations

```
You: "How is Liverpool doing this season?"
Agent: "Liverpool is currently 2nd in the Premier League..."

You: "What about their Champions League campaign?"
Agent: [Understands "their" = Liverpool, switches context to UCL]

You: "Compare them to last season"
Agent: [Compares Liverpool 2024 vs Liverpool 2023]
```

---

## 🎨 UI Components

### Chat Interface

The main screen features a conversational chat interface:

- **User Messages**: Your queries in rounded cards (right-aligned)
- **Agent Messages**: AI responses with typewriter effect (left-aligned)
- **Tool Badges**: Pills showing which APIs were called
- **Metadata**: Token count and response time
- **Thinking Indicator**: Shows current agent stage (Planning/Executing/Synthesizing)

### Refinement System

After each response, refine the output:

**Built-in Pills**:
- 🔄 **Rephrase**: Get a fresh perspective
- 📊 **More Detail**: Deeper analysis with more stats
- ✂️ **Simplify**: Condense to key points
- 🆚 **Compare**: Add comparison with another team/player

**Custom Pills**: Long-press "+" to create your own (e.g., "Include tactics", "Add historical context")

### Match Cards

Swipeable cards for fixtures:
- **Swipe Left** → View detailed stats overlay
- **Swipe Right** → Add to your watchlist
- **Tap** → Expand for lineups and events
- **Long-press** → Share match details

### Gesture Controls

- **Pull-to-refresh**: Update live scores
- **Swipe from edge**: Open navigation drawer
- **Long-press team name**: Set as favorite
- **Double-tap message**: Quick copy

---

## 📊 Analytics Dashboard

Access via **📊 icon** in top bar.

### Token Usage Chart
Line chart showing AI token consumption over time:
- Toggle between 7, 30, 90-day views
- Color-coded by provider (Cerebras/Groq/Gemini/OpenRouter)
- Displays average tokens per query

### Success Rate Donut
Circular chart showing query success rates:
- Green: Successful queries
- Orange: Partial results
- Red: Failed queries
- Per-provider breakdown on tap

### Response Time Bar Chart
Bar chart of average response times:
- Grouped by query complexity (Simple/Medium/Complex)
- Shows min/max/average
- Includes cache vs. non-cache comparison

### Cache Statistics
- **Hit Rate**: Percentage of cached responses
- **Storage Used**: Total cache size
- **Oldest Entry**: When cache started
- **Top Cached Queries**: Most frequently cached endpoints

---

## 🏗️ Architecture

### Tech Stack

| Layer | Technology |
|-------|-----------|
| **UI** | Jetpack Compose + Material 3 |
| **Architecture** | MVVM + MVI |
| **Async** | Kotlin Coroutines + Flow |
| **Networking** | Retrofit + OkHttp |
| **Database** | Room (SQLite) |
| **DI** | Hilt |
| **JSON** | Kotlinx Serialization |

### Agent Workflow

```
1. QUERY INPUT
   └→ User types: "When does Arsenal play next?"

2. PLANNING (AI)
   └→ Analyze query intent
   └→ Select tools: [search_team, get_fixtures]
   └→ Create execution plan

3. EXECUTION
   └→ Tool 1: search_team("Arsenal")
       ├─ Check cache (MISS)
       ├─ Call API-Football
       └─ Return team_id: 42
   
   └→ Tool 2: get_fixtures(team_id=42, next=1)
       ├─ Check cache (HIT)
       └─ Return next fixture

4. SYNTHESIS (AI)
   └→ Combine results
   └→ Generate natural language response

5. RENDER
   └→ Typewriter animation
   └→ Show tool badges
   └→ Display metadata
```

### Caching Strategy

| Data Type | TTL | Rationale |
|-----------|-----|-----------|
| Live scores | 30 seconds | Frequently changing |
| Upcoming fixtures | 6 hours | Updates daily |
| Past fixtures | ∞ | Never changes |
| League standings | 6 hours | Updates after each match |
| Player stats | 24 hours | Historical data |
| H2H records | ∞ | Immutable history |
| Team search | 7 days | Rarely changes |

---

## 🔒 Privacy & Security

### Data Protection
- ✅ **API Keys Encrypted**: AES-256-GCM encryption via EncryptedSharedPreferences
- ✅ **Local Processing**: All conversation history stored locally
- ✅ **No Telemetry**: Zero analytics or tracking
- ✅ **HTTPS Only**: All API calls over secure connections

### Permissions
- 🌐 **Internet**: Required for API calls
- 📳 **Vibrate**: Optional, for haptic feedback

**We never collect, store, or transmit personal data to third parties.**

---

## 🐛 Troubleshooting

### Common Issues

#### "API key required" error
**Solution**:
1. Go to Settings → AI Provider
2. Enter a valid API key
3. Tap "Test Connection" to verify

#### Rate limit errors
The app automatically handles rate limits:
1. Detects rate limit response
2. Shows dialog with fallback option
3. One-tap switch to alternative provider
4. Retries query automatically

**Prevention**:
- Configure multiple AI providers for seamless fallback
- Use Cerebras (highest rate limits)

#### "No internet connection"
**Solution**:
- Check your network connection
- Some cached data will still work offline
- Try enabling airplane mode, then disabling

#### Slow responses
**Potential causes**:
- API-Football rate limits (100 calls/day on free tier)
- Network latency
- Complex queries requiring many API calls

**Solutions**:
- Upgrade to API-Football paid tier
- Use simpler queries
- Check Analytics dashboard for bottlenecks

#### Cache not working
**Solution**:
1. Settings → Advanced → Clear Cache
2. Restart app
3. First queries will rebuild cache

### Debug Mode

Enable in Settings → Developer Options:
- View raw API responses
- See agent planning logs
- Monitor token usage in real-time
- Export logs for troubleshooting

### Reporting Issues

Found a bug? Please open an issue on [GitHub](https://github.com/yourusername/football-agent/issues) with:
- Device model and Android version
- Steps to reproduce
- Expected vs. actual behavior
- Screenshots (if applicable)
- Logcat output (if available)

---

## 🗺️ Roadmap

### v2.0 (Q2 2025)
- [ ] Voice input for hands-free queries
- [ ] Live match commentary with real-time updates
- [ ] Team comparison mode (side-by-side UI)
- [ ] Custom alert system (e.g., "Notify me when Liverpool scores")
- [ ] Betting odds integration
- [ ] Tablet optimization with split-screen

### v2.5 (Q3 2025)
- [ ] Multi-language support (Spanish, French, German)
- [ ] Fantasy football integration
- [ ] Video highlights (via YouTube API)
- [ ] Social features (share insights with friends)

### v3.0 (Q4 2025)
- [ ] On-device LLM (no API required)
- [ ] AR match visualizations
- [ ] Personalized news feed
- [ ] Advanced tactical analysis with heat maps

---

## 🤝 Contributing

Contributions are welcome! Please read our [Contributing Guide](CONTRIBUTING.md) first.

### Development Setup

```bash
# Clone repository
git clone https://github.com/yourusername/football-agent.git
cd football-agent

# Create feature branch
git checkout -b feature/your-feature-name

# Make changes and test
./gradlew test
./gradlew connectedAndroidTest

# Submit pull request
```

### Code Style
- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use ktlint for formatting: `./gradlew ktlintFormat`
- Write unit tests for new features

---

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 🙏 Acknowledgments

- **Inspired by**: [Socurate](https://github.com/NaimNajmios/Socurate) - Agentic design patterns
- **Powered by**:
    - [API-Football](https://www.api-football.com/) - Football data
    - [Cerebras](https://cerebras.ai/) - Ultra-fast AI inference
    - [Groq](https://groq.com/) - Fast LLM infrastructure
    - [Google Gemini](https://ai.google.dev/) - Reliable AI models
    - [OpenRouter](https://openrouter.ai/) - Model aggregation
- **Built with**:
    - [Jetpack Compose](https://developer.android.com/jetpack/compose)
    - [Material Design 3](https://m3.material.io/)

---

## 📧 Contact

- **GitHub Issues**: [Report bugs or request features](https://github.com/yourusername/football-agent/issues)
- **Email**: your.email@example.com
- **Twitter**: [@yourusername](https://twitter.com/yourusername)

---

**Made with ⚽ for football fans worldwide**

*Last updated: December 31, 2025*