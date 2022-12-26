package models;

public enum SpeleologistCycleSteps {
    RequestEnvironmentState,
    ReceiveEnvironmentStateResponse,
    NotifyNavigatorAboutEnvironmentState,
    ReceiveActionFromNavigator,
    SendActionToEnvironment,
    ReceiveActionAcceptance,
}
