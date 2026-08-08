// Talks to the ZoyaAccessibilityService running natively in the Sara AI app,
// exposed to this page as window.ZoyaNativeBridge (see AutomationJsBridge.kt
// and ZoyaVoiceWebViewScreen.kt). Same call signatures as before so the rest
// of the app (App.tsx, useLiveSession.ts) doesn't need to change.

interface ZoyaNativeBridgeInterface {
  isNative(): boolean;
  isAccessibilityServiceEnabled(): boolean;
  openAccessibilitySettings(): void;
  launchApp(appName: string): string;
  tapByText(text: string): string;
  tapAtCoordinates(x: number, y: number): string;
  typeText(text: string): string;
  scroll(direction: string): string;
  goBack(): string;
  goHome(): string;
  getScreenContent(): string;
  makeCall(number: string): string;
  sendSms(number: string, message: string): string;
  sendWhatsAppMessage(number: string | undefined, message: string): string;
}

declare global {
  interface Window {
    ZoyaNativeBridge?: ZoyaNativeBridgeInterface;
  }
}

const bridge = (): ZoyaNativeBridgeInterface | undefined =>
  typeof window !== "undefined" ? window.ZoyaNativeBridge : undefined;

const parseJson = (raw: string): any => {
  try {
    return JSON.parse(raw);
  } catch {
    return { success: false };
  }
};

/** True only when running inside the Sara AI app with the native bridge present. */
export const isNativeAndroid = (): boolean => bridge()?.isNative() === true;

export const ZoyaAutomation = {
  async isAccessibilityServiceEnabled(): Promise<{ enabled: boolean }> {
    return { enabled: bridge()?.isAccessibilityServiceEnabled() === true };
  },
  async openAccessibilitySettings(): Promise<void> {
    bridge()?.openAccessibilitySettings();
  },
  async launchApp(options: { appName: string }): Promise<{ success: boolean }> {
    return parseJson(bridge()?.launchApp(options.appName) ?? "{}");
  },
  async tapByText(options: { text: string }): Promise<{ success: boolean }> {
    return parseJson(bridge()?.tapByText(options.text) ?? "{}");
  },
  async tapAtCoordinates(options: { x: number; y: number }): Promise<{ success: boolean }> {
    return parseJson(bridge()?.tapAtCoordinates(options.x, options.y) ?? "{}");
  },
  async typeText(options: { text: string }): Promise<{ success: boolean }> {
    return parseJson(bridge()?.typeText(options.text) ?? "{}");
  },
  async scroll(options: { direction: string }): Promise<{ success: boolean }> {
    return parseJson(bridge()?.scroll(options.direction) ?? "{}");
  },
  async goBack(): Promise<void> {
    bridge()?.goBack();
  },
  async goHome(): Promise<void> {
    bridge()?.goHome();
  },
  async getScreenContent(): Promise<{ content: string }> {
    return parseJson(bridge()?.getScreenContent() ?? "{}");
  },
  async makeCall(options: { number: string }): Promise<{ success: boolean }> {
    return parseJson(bridge()?.makeCall(options.number) ?? "{}");
  },
  async sendSms(options: { number: string; message: string }): Promise<{ success: boolean }> {
    return parseJson(bridge()?.sendSms(options.number, options.message) ?? "{}");
  },
  async sendWhatsAppMessage(options: {
    number?: string;
    contactName?: string;
    message: string;
  }): Promise<{ success: boolean; message: string }> {
    const res = parseJson(bridge()?.sendWhatsAppMessage(options.number, options.message) ?? "{}");
    return { success: res.success === true, message: options.message };
  },
};
