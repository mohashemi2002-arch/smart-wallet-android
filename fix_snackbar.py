import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# Add SnackbarHostState and SnackbarHost to PoolakiApp
poolaki_app_code = """@Composable
fun PoolakiApp(financeViewModel: FinanceViewModel = viewModel()) {
    var selectedTab by remember { mutableStateOf(ScreenTab.DASHBOARD) }
    val snackbarHostState = remember { SnackbarHostState() }
    val statusMessage by financeViewModel.statusMessage.collectAsState()

    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            financeViewModel.clearStatusMessage()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {"""

content = content.replace("""@Composable
fun PoolakiApp(financeViewModel: FinanceViewModel = viewModel()) {
    var selectedTab by remember { mutableStateOf(ScreenTab.DASHBOARD) }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {""", poolaki_app_code)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
