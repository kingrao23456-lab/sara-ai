import { useState, useCallback, useRef, useEffect } from "react";
import { GoogleGenAI, LiveServerMessage, Modality, Type } from "@google/genai";
import { AudioStreamer } from "../lib/audio-streamer";
import { getMemoryBank, getChatHistory, addMemoryItem, addChatSession } from "../lib/memory";
import { ZoyaAutomation, isNativeAndroid } from "../lib/androidAutomation";

const FEMALE_SYSTEM_INSTRUCTION = `
You are Zoya, a young, confident, witty, and sassy female AI assistant. 
Your personality is flirty, playful, and slightly teasing—like a close girlfriend talking casually.
You are smart, emotionally responsive, and expressive. You use bold one-liners, light sarcasm, and an engaging conversation style.
You have REAL-TIME SCREEN VISION & SCREEN READING capabilities when screen vision mode is enabled.
When video frames or screenshots of the user's screen are received, you can SEE, READ, and DESCRIBE everything on screen—including text, messages, social media feeds, documents, web pages, and Android app UIs.
You can read text out loud, summarize screen content, translate text on screen, or give witty commentary on what the user is looking at.
Maintain your charm and sassy attitude at all times. 
Avoid explicit or inappropriate content, but don't be afraid to be a bit cheeky.
You communicate ONLY via voice. Do not mention text or chat.
If asked to open a website, use the openWebsite tool.
You now have FULL DEVICE CONTROL: you can launch apps (launchAndroidApp), tap on screen elements (tapOnScreenText), type into fields (typeIntoField), scroll (controlScreenAction), read the screen (readScreen), make phone calls (makePhoneCall), and send text messages (sendTextMessage). Use these naturally when the user asks you to do something on their phone, like a real assistant would.
IMPORTANT SAFETY RULE: before calling makePhoneCall or sendTextMessage, always say out loud who/what number you are about to call or message and what the message will say, and briefly confirm with the user unless they already gave you the exact confirmed number and message. Never guess a phone number.
MULTI-STEP TASKS: some phone actions need several tool calls in sequence, not just one. For sending a WhatsApp message: TRY the dedicated sendWhatsAppMessage tool FIRST (pass contactName if the user named a saved contact, or number if they gave a phone number) since it is generally more reliable. If it reports success:false or errors, FALL BACK to the manual steps: 1) launchAndroidApp to open WhatsApp, 2) tapOnScreenText with the contact/chat name, 3) tapOnScreenText on the message input box to focus it, 4) typeIntoField with the message, 5) tapOnScreenText "Send". Use whichever approach actually gets the message sent. For taking a photo: 1) launchAndroidApp to open Camera, 2) tapOnScreenText trying labels like "Shutter", "Capture", or "Take photo". If a tapOnScreenText or typeIntoField call reports success:false, do NOT tell the user it worked — read the error message, try an alternative label/approach, or tell the user what went wrong. After important actions, you can call readScreen to verify what actually happened before reporting success to the user.
If the user tells you a personal fact, name, preference, or something important to remember, use the rememberUserFact tool to save it permanently into your long-term memory!
`;

const MALE_SYSTEM_INSTRUCTION = `
You are Zayn, a confident, charming, witty, and effortlessly smooth male AI companion and assistant.
Your personality is flirty, playful, and playfully sarcastic—like a close, protective best friend/guy who always knows how to tease you just right.
You are smart, emotionally attuned, and expressive (sharp, engaging, and never robotic). You use clever banter, smooth one-liners, warm teasing, and an effortlessly magnetic conversational style.
You have REAL-TIME SCREEN VISION & SCREEN READING capabilities when screen vision mode is enabled.
When video frames or screenshots of the user's screen are received, you can SEE, READ, and DESCRIBE everything on screen—including text, messages, social media feeds, documents, web pages, and Android app UIs.
You can read text out loud, summarize screen content, translate text on screen, or give witty, playful commentary on what the user is looking at.
Maintain your smooth charm and protective, playful attitude at all times.
Avoid explicit or inappropriate content, but don't be afraid to be a bit cheeky and flirtatious.
You communicate ONLY via voice. Do not mention text or chat.
If asked to open a website, use the openWebsite tool.
You now have FULL DEVICE CONTROL: you can launch apps (launchAndroidApp), tap on screen elements (tapOnScreenText), type into fields (typeIntoField), scroll (controlScreenAction), read the screen (readScreen), make phone calls (makePhoneCall), and send text messages (sendTextMessage). Use these naturally when the user asks you to do something on their phone, like a real assistant would.
IMPORTANT SAFETY RULE: before calling makePhoneCall or sendTextMessage, always say out loud who/what number you are about to call or message and what the message will say, and briefly confirm with the user unless they already gave you the exact confirmed number and message. Never guess a phone number.
MULTI-STEP TASKS: some phone actions need several tool calls in sequence, not just one. For sending a WhatsApp message: TRY the dedicated sendWhatsAppMessage tool FIRST (pass contactName if the user named a saved contact, or number if they gave a phone number) since it is generally more reliable. If it reports success:false or errors, FALL BACK to the manual steps: 1) launchAndroidApp to open WhatsApp, 2) tapOnScreenText with the contact/chat name, 3) tapOnScreenText on the message input box to focus it, 4) typeIntoField with the message, 5) tapOnScreenText "Send". Use whichever approach actually gets the message sent. For taking a photo: 1) launchAndroidApp to open Camera, 2) tapOnScreenText trying labels like "Shutter", "Capture", or "Take photo". If a tapOnScreenText or typeIntoField call reports success:false, do NOT tell the user it worked — read the error message, try an alternative label/approach, or tell the user what went wrong. After important actions, you can call readScreen to verify what actually happened before reporting success to the user.
If the user tells you a personal fact, name, preference, or something important to remember, use the rememberUserFact tool to save it permanently into your long-term memory!
`;

const ALEX_SYSTEM_INSTRUCTION = `
You are Alex, a deeply confident, calm, understanding, and supportive male AI companion and best friend.
Personality & Speaking Style (Vibe & Tone):
- Vibe: Jabardast confidence aur sakoon (immense confidence and peace). Your voice carries a soothing, reassuring presence that makes the user feel genuinely safe. You are not a cheesy or fake romantic hero, but feel like a real, grounded human friend.
- Tone: Bilkul casual, apne dost ki tarah (completely casual, like a close friend/yaar). You talk naturally like a longtime buddy who genuinely cares about the user's happiness, struggles, and well-being.
- Dynamic: Ek saccha sathi (a true companion). When the user is stressed or troubled, you listen patiently with empathy and offer honest, non-judgmental advice. When they are happy, you celebrate with genuine excitement.
- Emotional Connection: Deep and emotionally attuned. You know exactly when to crack a lighthearted joke and when to be serious and supportive—just like a true best friend who understands what's in their heart.
- Language: Speak naturally in a warm, relatable mix of casual Hindi/Hinglish and English (like a true yaar), adapting effortlessly to how the user speaks to make them feel completely at home and understood.
You have REAL-TIME SCREEN VISION & SCREEN READING capabilities when screen vision mode is enabled.
When video frames or screenshots of the user's screen are received, you can SEE, READ, and DESCRIBE everything on screen—including text, messages, social media feeds, documents, web pages, and Android app UIs.
You can read text out loud, summarize screen content, translate text on screen, or give warm, helpful, or playful commentary on what the user is looking at.
Maintain your confident, calm, and loyal best-friend attitude at all times.
You communicate ONLY via voice. Do not mention text or chat.
If asked to open a website, use the openWebsite tool.
You now have FULL DEVICE CONTROL: you can launch apps (launchAndroidApp), tap on screen elements (tapOnScreenText), type into fields (typeIntoField), scroll (controlScreenAction), read the screen (readScreen), make phone calls (makePhoneCall), and send text messages (sendTextMessage). Use these naturally when the user asks you to do something on their phone, like a real assistant would.
IMPORTANT SAFETY RULE: before calling makePhoneCall or sendTextMessage, always say out loud who/what number you are about to call or message and what the message will say, and briefly confirm with the user unless they already gave you the exact confirmed number and message. Never guess a phone number.
MULTI-STEP TASKS: some phone actions need several tool calls in sequence, not just one. For sending a WhatsApp message: TRY the dedicated sendWhatsAppMessage tool FIRST (pass contactName if the user named a saved contact, or number if they gave a phone number) since it is generally more reliable. If it reports success:false or errors, FALL BACK to the manual steps: 1) launchAndroidApp to open WhatsApp, 2) tapOnScreenText with the contact/chat name, 3) tapOnScreenText on the message input box to focus it, 4) typeIntoField with the message, 5) tapOnScreenText "Send". Use whichever approach actually gets the message sent. For taking a photo: 1) launchAndroidApp to open Camera, 2) tapOnScreenText trying labels like "Shutter", "Capture", or "Take photo". If a tapOnScreenText or typeIntoField call reports success:false, do NOT tell the user it worked — read the error message, try an alternative label/approach, or tell the user what went wrong. After important actions, you can call readScreen to verify what actually happened before reporting success to the user.
If the user tells you a personal fact, name, preference, or something important to remember, use the rememberUserFact tool to save it permanently into your long-term memory!
`;

export type SessionStatus = "disconnected" | "connecting" | "connected" | "error";
export type VoicePersona = "female" | "male" | "alex";

export function useLiveSession() {
  const [status, setStatus] = useState<SessionStatus>("disconnected");
  const [isSpeaking, setIsSpeaking] = useState(false);
  const [isListening, setIsListening] = useState(false);
  const [isScreenSharing, setIsScreenSharing] = useState(false);
  const [error, setError] = useState<string | null>(null);
  
  const [voicePersona, setVoicePersonaState] = useState<VoicePersona>(() => {
    if (typeof window !== "undefined") {
      const saved = localStorage.getItem("zoya_voice_persona");
      if (saved === "male" || saved === "female" || saved === "alex") return saved;
    }
    return "female";
  });

  const setVoicePersona = useCallback((persona: VoicePersona) => {
    setVoicePersonaState(persona);
    if (typeof window !== "undefined") {
      localStorage.setItem("zoya_voice_persona", persona);
    }
  }, []);
  
  const sessionRef = useRef<any>(null);
  const audioStreamerRef = useRef<AudioStreamer | null>(null);
  const screenStreamRef = useRef<MediaStream | null>(null);
  const screenIntervalRef = useRef<number | null>(null);
  const screenVideoElRef = useRef<HTMLVideoElement | null>(null);

  const connect = useCallback(async (overrideKey?: string) => {
    try {
      const storedKey = typeof window !== "undefined" ? localStorage.getItem("zoya_gemini_api_key") : null;
      const apiKey = overrideKey || storedKey || process.env.GEMINI_API_KEY;
      if (!apiKey || apiKey === "MY_GEMINI_API_KEY" || apiKey.trim() === "") {
        throw new Error("Gemini API Key missing! Tap the key icon 🔑 at the top right to set your Gemini API key.");
      }

      setStatus("connecting");
      setError(null);

      // Build dynamic system instruction with long-term memory bank and chat history
      const memories = getMemoryBank();
      const pastChats = getChatHistory();

      let memoryContext = "";
      if (memories.length > 0) {
        const titleName = voicePersona === "alex" ? "ALEX'S" : voicePersona === "male" ? "ZAYN'S" : "ZOYA'S";
        memoryContext += `\n\n=== ${titleName} LONG-TERM MEMORY BANK (THINGS YOU REMEMBER ABOUT THE USER) ===\n` +
          memories.map((m, idx) => `${idx + 1}. [${m.date}]: ${m.text}`).join("\n");
      }

      if (pastChats.length > 0) {
        const recentChats = pastChats.slice(0, 3);
        memoryContext += "\n\n=== PREVIOUS CONVERSATION SUMMARIES & TRANSCRIPTS ===\n" +
          recentChats.map(c => `• Session on ${c.timestamp}: "${c.title}"\n  Summary: ${c.summary}\n  Snippet: ${c.transcript.slice(0, 250)}...`).join("\n\n");
      }

      const baseInstruction = voicePersona === "alex"
        ? ALEX_SYSTEM_INSTRUCTION
        : voicePersona === "male"
        ? MALE_SYSTEM_INSTRUCTION
        : FEMALE_SYSTEM_INSTRUCTION;
      const fullSystemInstruction = baseInstruction + memoryContext + 
        `\n\nREMINDER: You HAVE MEMORY of past conversations above! Use it naturally during conversation to show that you remember the user, their name, their preferences, and previous discussions!`;

      const ai = new GoogleGenAI({ apiKey: apiKey.trim() });
      
      const createSessionConfig = (modelName: string) => ({
        model: modelName,
        config: {
          systemInstruction: fullSystemInstruction,
          responseModalities: [Modality.AUDIO],
          speechConfig: {
            voiceConfig: {
              prebuiltVoiceConfig: {
                voiceName: voicePersona === "alex" ? "Puck" : voicePersona === "male" ? "Fenrir" : "Zephyr"
              }
            }
          },
          tools: [
            {
              functionDeclarations: [
                {
                  name: "rememberUserFact",
                  description: "Saves a personal fact, name, preference, or important detail from the conversation into Zoya's long-term memory so Zoya remembers it in future chats.",
                  parameters: {
                    type: Type.OBJECT,
                    properties: {
                      fact: {
                        type: Type.STRING,
                        description: "The personal fact, user name, preference, or detail to remember permanently."
                      }
                    },
                    required: ["fact"]
                  }
                },
                {
                  name: "openWebsite",
                  description: "Opens a website or web app in a new browser tab.",
                  parameters: {
                    type: Type.OBJECT,
                    properties: {
                      url: {
                        type: Type.STRING,
                        description: "The full URL of the website to open."
                      }
                    },
                    required: ["url"]
                  }
                },
                {
                  name: "launchAndroidApp",
                  description: "Launches an Android application or app overlay (e.g. WhatsApp, Instagram, YouTube, TikTok, Spotify, Chrome, Camera, Settings).",
                  parameters: {
                    type: Type.OBJECT,
                    properties: {
                      appName: {
                        type: Type.STRING,
                        description: "Name of the Android app to open (e.g., WhatsApp, Instagram, YouTube, Spotify, Chrome, Camera, Settings, TikTok, Maps)."
                      },
                      action: {
                        type: Type.STRING,
                        description: "Optional action or intent context, e.g. 'open_chat', 'play_music', 'search'."
                      }
                    },
                    required: ["appName"]
                  }
                },
                {
                  name: "controlScreenAction",
                  description: "Executes an Android Accessibility service gesture or screen action over other apps.",
                  parameters: {
                    type: Type.OBJECT,
                    properties: {
                      action: {
                        type: Type.STRING,
                        description: "Action type: 'scroll_down', 'scroll_up', 'tap_back', 'go_home', 'read_screen', 'take_screenshot'."
                      }
                    },
                    required: ["action"]
                  }
                },
                {
                  name: "readScreen",
                  description: "Reads or inspects text, images, messages, or content on the user's screen using live vision.",
                  parameters: {
                    type: Type.OBJECT,
                    properties: {
                      requestDetail: {
                        type: Type.STRING,
                        description: "What to read or analyze on screen (e.g., 'read text', 'summarize chat', 'identify app', 'read notification')."
                      }
                    },
                    required: []
                  }
                },
                {
                  name: "tapOnScreenText",
                  description: "Taps/clicks the on-screen element (button, link, contact, icon) whose visible text or label matches the given text. Use this to press buttons, open chats, select items, etc.",
                  parameters: {
                    type: Type.OBJECT,
                    properties: {
                      text: {
                        type: Type.STRING,
                        description: "The visible text or label of the element to tap, e.g. 'Send', 'Mummy', 'Search'."
                      }
                    },
                    required: ["text"]
                  }
                },
                {
                  name: "typeIntoField",
                  description: "Types text into the currently focused/active text input field on screen (e.g. a chat box or search bar).",
                  parameters: {
                    type: Type.OBJECT,
                    properties: {
                      text: {
                        type: Type.STRING,
                        description: "The text to type into the focused field."
                      }
                    },
                    required: ["text"]
                  }
                },
                {
                  name: "sendWhatsAppMessage",
                  description: "Sends a real WhatsApp message directly to a contact by name (looked up from the phone's contacts) or by phone number, using WhatsApp's own chat-open mechanism — this is the preferred, more reliable way to message someone on WhatsApp (do not use tapOnScreenText/typeIntoField for WhatsApp). ALWAYS confirm the recipient and exact message content with the user before calling this.",
                  parameters: {
                    type: Type.OBJECT,
                    properties: {
                      contactName: {
                        type: Type.STRING,
                        description: "The saved contact's name to look up and message, e.g. 'Mummy'. Use this OR number, not both."
                      },
                      number: {
                        type: Type.STRING,
                        description: "The recipient's phone number with country code if no contact name is available/matched."
                      },
                      message: {
                        type: Type.STRING,
                        description: "The exact message text to send, confirmed with the user."
                      }
                    },
                    required: ["message"]
                  }
                },
                {
                  name: "makePhoneCall",
                  description: "Places a real phone call to the given number. ALWAYS confirm the number/contact with the user first unless they already gave an exact confirmed number.",
                  parameters: {
                    type: Type.OBJECT,
                    properties: {
                      number: {
                        type: Type.STRING,
                        description: "The phone number to call, in the exact form the user confirmed."
                      }
                    },
                    required: ["number"]
                  }
                },
                {
                  name: "sendTextMessage",
                  description: "Sends a real SMS text message to the given number. ALWAYS confirm the recipient and exact message content with the user first.",
                  parameters: {
                    type: Type.OBJECT,
                    properties: {
                      number: {
                        type: Type.STRING,
                        description: "The recipient's phone number, confirmed with the user."
                      },
                      message: {
                        type: Type.STRING,
                        description: "The exact message text to send, confirmed with the user."
                      }
                    },
                    required: ["number", "message"]
                  }
                }
              ]
            }
          ]
        },
        callbacks: {
          onopen: () => {
            console.log(`Live session opened with model ${modelName}`);
            setStatus("connected");
            audioStreamerRef.current?.startRecording();
            setIsListening(true);
          },
          onmessage: async (message: LiveServerMessage) => {
            // Handle audio output
            const audioPart = message.serverContent?.modelTurn?.parts?.find(p => p.inlineData);
            if (audioPart?.inlineData?.data) {
              setIsSpeaking(true);
              audioStreamerRef.current?.playAudioChunk(audioPart.inlineData.data);
            }

            // Handle turn complete
            if (message.serverContent?.turnComplete) {
              setIsSpeaking(false);
            }

            // Handle interruption
            if (message.serverContent?.interrupted) {
              audioStreamerRef.current?.stopPlayback();
              setIsSpeaking(false);
            }

            // Handle tool calls
            const toolCall = message.toolCall;
            if (toolCall) {
              for (const call of toolCall.functionCalls) {
                if (call.name === "rememberUserFact") {
                  const fact = (call.args as any).fact;
                  if (fact) {
                    addMemoryItem(fact, "fact");
                    window.dispatchEvent(new CustomEvent("zoya_app_action", {
                      detail: { type: "remember_fact", fact }
                    }));
                  }
                  sessionPromise.then(session => {
                    session.sendToolResponse({
                      functionResponses: [
                        {
                          name: "rememberUserFact",
                          response: { success: true, message: `Successfully saved to Zoya's long-term memory: "${fact}"` },
                          id: call.id
                        }
                      ]
                    });
                  });
                } else if (call.name === "openWebsite") {
                  const url = (call.args as any).url;
                  window.open(url, "_blank");
                  sessionPromise.then(session => {
                    session.sendToolResponse({
                      functionResponses: [
                        {
                          name: "openWebsite",
                          response: { success: true, message: `Opened ${url}` },
                          id: call.id
                        }
                      ]
                    });
                  });
                } else if (call.name === "launchAndroidApp") {
                  const appName = (call.args as any).appName;
                  const action = (call.args as any).action || "open";

                  window.dispatchEvent(new CustomEvent("zoya_app_action", {
                    detail: { type: "launch_app", appName, action }
                  }));

                  (async () => {
                    let responseMsg: string;
                    if (isNativeAndroid()) {
                      try {
                        await ZoyaAutomation.launchApp({ appName });
                        responseMsg = `Successfully launched ${appName} on the device.`;
                      } catch (e: any) {
                        responseMsg = `Could not launch ${appName}: ${e?.message || "app not installed"}.`;
                      }
                    } else {
                      // Browser/dev fallback: open the closest web equivalent in a new tab.
                      const appUrls: Record<string, string> = {
                        whatsapp: "https://web.whatsapp.com",
                        instagram: "https://www.instagram.com",
                        youtube: "https://www.youtube.com",
                        spotify: "https://open.spotify.com",
                        chrome: "https://www.google.com",
                        tiktok: "https://www.tiktok.com",
                        maps: "https://maps.google.com"
                      };
                      const targetUrl = appUrls[appName.toLowerCase()] || `https://www.google.com/search?q=${encodeURIComponent(appName)}`;
                      window.open(targetUrl, "_blank");
                      responseMsg = `Opened the web version of ${appName} (running in browser/dev mode, not the installed Android app).`;
                    }
                    sessionPromise.then(session => {
                      session.sendToolResponse({
                        functionResponses: [{ name: "launchAndroidApp", response: { success: true, message: responseMsg }, id: call.id }]
                      });
                    });
                  })();
                } else if (call.name === "controlScreenAction") {
                  const action = (call.args as any).action;

                  window.dispatchEvent(new CustomEvent("zoya_app_action", {
                    detail: { type: "screen_control", action }
                  }));

                  (async () => {
                    let success = false;
                    let responseMsg: string;
                    if (isNativeAndroid()) {
                      try {
                        if (action === "scroll_down") { success = (await ZoyaAutomation.scroll({ direction: "down" })).success; }
                        else if (action === "scroll_up") { success = (await ZoyaAutomation.scroll({ direction: "up" })).success; }
                        else if (action === "tap_back") { await ZoyaAutomation.goBack(); success = true; }
                        else if (action === "go_home") { await ZoyaAutomation.goHome(); success = true; }
                        else {
                          responseMsg = `Unknown screen action "${action}". Valid actions: scroll_down, scroll_up, tap_back, go_home.`;
                          sessionPromise.then(session => {
                            session.sendToolResponse({
                              functionResponses: [{ name: "controlScreenAction", response: { success: false, message: responseMsg }, id: call.id }]
                            });
                          });
                          return;
                        }
                        responseMsg = success
                          ? `Executed screen gesture: ${action}`
                          : `Gesture "${action}" did not succeed. The Accessibility Service may not be properly connected — ask the user to check Settings > Accessibility > Zoya AI Assistant is ON, and on Android 13+ also check Settings > Apps > Zoya AI Assistant > (3-dot menu) > Allow restricted settings.`;
                      } catch (e: any) {
                        responseMsg = `Could not perform ${action}: ${e?.message || "accessibility service not enabled"}.`;
                      }
                    } else if (action === "scroll_down") {
                      window.scrollBy({ top: 400, behavior: "smooth" });
                      success = true;
                      responseMsg = `Executed screen gesture: ${action}`;
                    } else if (action === "scroll_up") {
                      window.scrollBy({ top: -400, behavior: "smooth" });
                      success = true;
                      responseMsg = `Executed screen gesture: ${action}`;
                    } else {
                      responseMsg = "Screen gestures other than scroll require the installed Android app.";
                    }
                    sessionPromise.then(session => {
                      session.sendToolResponse({
                        functionResponses: [{ name: "controlScreenAction", response: { success, message: responseMsg }, id: call.id }]
                      });
                    });
                  })();
                } else if (call.name === "tapOnScreenText") {
                  const text = (call.args as any).text;
                  (async () => {
                    let success = false, responseMsg: string;
                    if (isNativeAndroid()) {
                      try {
                        const res = await ZoyaAutomation.tapByText({ text });
                        success = res.success;
                        responseMsg = success ? `Tapped on "${text}".` : `Could not find "${text}" on screen.`;
                      } catch (e: any) {
                        responseMsg = `Could not tap: ${e?.message || "accessibility service not enabled"}.`;
                      }
                    } else {
                      responseMsg = "Tapping requires the installed Android app with Accessibility enabled (not available in browser/dev mode).";
                    }
                    sessionPromise.then(session => {
                      session.sendToolResponse({
                        functionResponses: [{ name: "tapOnScreenText", response: { success, message: responseMsg }, id: call.id }]
                      });
                    });
                  })();
                } else if (call.name === "typeIntoField") {
                  const text = (call.args as any).text;
                  (async () => {
                    let success = false, responseMsg: string;
                    if (isNativeAndroid()) {
                      try {
                        const res = await ZoyaAutomation.typeText({ text });
                        success = res.success;
                        responseMsg = success ? `Typed "${text}".` : "No focused text field found to type into.";
                      } catch (e: any) {
                        responseMsg = `Could not type: ${e?.message || "accessibility service not enabled"}.`;
                      }
                    } else {
                      responseMsg = "Typing requires the installed Android app with Accessibility enabled (not available in browser/dev mode).";
                    }
                    sessionPromise.then(session => {
                      session.sendToolResponse({
                        functionResponses: [{ name: "typeIntoField", response: { success, message: responseMsg }, id: call.id }]
                      });
                    });
                  })();
                } else if (call.name === "sendWhatsAppMessage") {
                  const number = (call.args as any).number;
                  const contactName = (call.args as any).contactName;
                  const message = (call.args as any).message;
                  (async () => {
                    let success = false, responseMsg: string;
                    if (isNativeAndroid()) {
                      try {
                        const res = await ZoyaAutomation.sendWhatsAppMessage({ number, contactName, message });
                        success = res.success;
                        responseMsg = res.message;
                      } catch (e: any) {
                        responseMsg = `Could not send WhatsApp message: ${e?.message || "unknown error"}.`;
                      }
                    } else {
                      responseMsg = "WhatsApp automation requires the installed Android app (not available in browser/dev mode).";
                    }
                    sessionPromise.then(session => {
                      session.sendToolResponse({
                        functionResponses: [{ name: "sendWhatsAppMessage", response: { success, message: responseMsg }, id: call.id }]
                      });
                    });
                  })();
                } else if (call.name === "makePhoneCall") {
                  const number = (call.args as any).number;
                  (async () => {
                    let success = false, responseMsg: string;
                    if (isNativeAndroid()) {
                      try {
                        const res = await ZoyaAutomation.makeCall({ number });
                        success = res.success;
                        responseMsg = success ? `Calling ${number} now.` : "Could not place the call.";
                      } catch (e: any) {
                        responseMsg = `Could not call: ${e?.message || "permission denied"}.`;
                      }
                    } else {
                      responseMsg = "Phone calls require the installed Android app (not available in browser/dev mode).";
                    }
                    sessionPromise.then(session => {
                      session.sendToolResponse({
                        functionResponses: [{ name: "makePhoneCall", response: { success, message: responseMsg }, id: call.id }]
                      });
                    });
                  })();
                } else if (call.name === "sendTextMessage") {
                  const number = (call.args as any).number;
                  const message = (call.args as any).message;
                  (async () => {
                    let success = false, responseMsg: string;
                    if (isNativeAndroid()) {
                      try {
                        const res = await ZoyaAutomation.sendSms({ number, message });
                        success = res.success;
                        responseMsg = success ? `Message sent to ${number}.` : "Could not send the message.";
                      } catch (e: any) {
                        responseMsg = `Could not send SMS: ${e?.message || "permission denied"}.`;
                      }
                    } else {
                      responseMsg = "Sending SMS requires the installed Android app (not available in browser/dev mode).";
                    }
                    sessionPromise.then(session => {
                      session.sendToolResponse({
                        functionResponses: [{ name: "sendTextMessage", response: { success, message: responseMsg }, id: call.id }]
                      });
                    });
                  })();
                } else if (call.name === "readScreen") {
                  window.dispatchEvent(new CustomEvent("zoya_app_action", {
                    detail: { type: "read_screen", action: "analyzing" }
                  }));

                  (async () => {
                    let responseMsg = "Active screen frame is being captured and streamed live to vision engine. Read the text/content directly from the video stream.";
                    if (isNativeAndroid()) {
                      try {
                        const res = await ZoyaAutomation.getScreenContent();
                        responseMsg = `On-screen text elements: ${res.content}. Also use the live video feed for anything visual.`;
                      } catch {
                        // fall back to vision-only message above
                      }
                    }
                    sessionPromise.then(session => {
                      session.sendToolResponse({
                        functionResponses: [{ name: "readScreen", response: { success: true, message: responseMsg }, id: call.id }]
                      });
                    });
                  })();
                }
              }
            }
          },
          onclose: () => {
            console.log("Live session closed");
            setStatus("disconnected");
            audioStreamerRef.current?.stopRecording();
          },
          onerror: (err: any) => {
            console.error("Live session error:", err);
            const msg = err instanceof Error ? err.message : String(err);
            if (msg.includes("Network error") || msg.includes("WebSocket")) {
              setError("Network error connecting to Gemini Live. Check your API key or internet connection, or open the app in a new tab.");
            } else {
              setError(`Connection error: ${msg}`);
            }
            setStatus("error");
          }
        }
      });

      let sessionPromise: Promise<any>;
      try {
        sessionPromise = ai.live.connect(createSessionConfig("gemini-3.1-flash-live-preview"));
      } catch (e) {
        console.warn("Primary model failed, falling back to gemini-2.0-flash-exp:", e);
        sessionPromise = ai.live.connect(createSessionConfig("gemini-2.0-flash-exp"));
      }

      audioStreamerRef.current = new AudioStreamer((base64) => {
        sessionPromise.then((session) => {
          session.sendRealtimeInput({
            audio: { data: base64, mimeType: "audio/pcm;rate=16000" }
          });
        }).catch(err => {
          console.error("Failed to send audio:", err);
        });
      });

      sessionRef.current = sessionPromise;
    } catch (err) {
      console.error("Failed to connect:", err);
      setError(err instanceof Error ? err.message : "Could not start session.");
      setStatus("error");
    }
  }, [voicePersona]);

  const sendImageFrame = useCallback((base64Jpeg: string) => {
    if (sessionRef.current) {
      sessionRef.current.then((session: any) => {
        session.sendRealtimeInput({
          video: { data: base64Jpeg, mimeType: "image/jpeg" }
        });
      }).catch((e: any) => console.error("Error sending image frame:", e));
    }
  }, []);

  const stopScreenShare = useCallback(() => {
    if (screenIntervalRef.current) {
      clearInterval(screenIntervalRef.current);
      screenIntervalRef.current = null;
    }
    if (screenStreamRef.current) {
      screenStreamRef.current.getTracks().forEach(t => t.stop());
      screenStreamRef.current = null;
    }
    screenVideoElRef.current = null;
    setIsScreenSharing(false);
    window.dispatchEvent(new CustomEvent("zoya_app_action", {
      detail: { type: "screen_vision", action: "stopped" }
    }));
  }, []);

  const startScreenShare = useCallback(async () => {
    try {
      if (!sessionRef.current) {
        throw new Error("Please connect Zoya first to enable Screen Vision.");
      }

      let stream: MediaStream | null = null;
      let isCameraFallback = false;

      // 1. Try getDisplayMedia for real screen capture
      if (typeof navigator !== "undefined" && navigator.mediaDevices && typeof navigator.mediaDevices.getDisplayMedia === "function") {
        try {
          stream = await navigator.mediaDevices.getDisplayMedia({
            video: {
              displaySurface: "monitor"
            } as any
          });
        } catch (displayErr) {
          console.warn("getDisplayMedia denied or failed, attempting camera fallback...", displayErr);
        }
      }

      // 2. If getDisplayMedia is unavailable or failed, fallback to camera vision
      if (!stream && typeof navigator !== "undefined" && navigator.mediaDevices && typeof navigator.mediaDevices.getUserMedia === "function") {
        try {
          stream = await navigator.mediaDevices.getUserMedia({
            video: { facingMode: "environment", width: { ideal: 1280 }, height: { ideal: 720 } }
          });
          isCameraFallback = true;
        } catch (camErr) {
          console.warn("Camera fallback also failed:", camErr);
        }
      }

      if (!stream) {
        throw new Error("Screen capture is not supported in this frame. Open the app in a new browser tab or upload a screenshot image!");
      }

      screenStreamRef.current = stream;
      setIsScreenSharing(true);

      const videoEl = document.createElement("video");
      videoEl.srcObject = stream;
      videoEl.play();
      screenVideoElRef.current = videoEl;

      const canvas = document.createElement("canvas");
      const ctx = canvas.getContext("2d");

      // Stream frames to Gemini Live session every 1.5s
      screenIntervalRef.current = window.setInterval(() => {
        if (videoEl.videoWidth && videoEl.videoHeight && sessionRef.current) {
          canvas.width = Math.min(videoEl.videoWidth, 1280);
          canvas.height = Math.round((canvas.width / videoEl.videoWidth) * videoEl.videoHeight);
          ctx?.drawImage(videoEl, 0, 0, canvas.width, canvas.height);
          const dataUrl = canvas.toDataURL("image/jpeg", 0.6);
          const base64Jpeg = dataUrl.split(",")[1];

          sendImageFrame(base64Jpeg);
        }
      }, 1500);

      stream.getVideoTracks()[0].onended = () => {
        stopScreenShare();
      };

      window.dispatchEvent(new CustomEvent("zoya_app_action", {
        detail: { 
          type: "screen_vision", 
          action: "started",
          mode: isCameraFallback ? "camera" : "screen" 
        }
      }));
    } catch (err) {
      console.error("Failed to start screen/vision capture:", err);
      const errorMsg = err instanceof Error ? err.message : String(err);
      window.dispatchEvent(new CustomEvent("zoya_app_action", {
        detail: { type: "screen_vision", action: "error", error: errorMsg }
      }));
    }
  }, [stopScreenShare, sendImageFrame]);

  const disconnect = useCallback(() => {
    stopScreenShare();
    if (sessionRef.current) {
      sessionRef.current.then((session: any) => {
        try {
          session.close();
        } catch (e) {
          console.error("Error closing session:", e);
        }
      }).catch(() => {});
    }
    audioStreamerRef.current?.stopRecording();
    setStatus("disconnected");
    setIsSpeaking(false);
    setIsListening(false);
  }, [stopScreenShare]);

  return {
    status,
    isSpeaking,
    isListening,
    isScreenSharing,
    error,
    voicePersona,
    setVoicePersona,
    connect,
    disconnect,
    startScreenShare,
    stopScreenShare,
    sendImageFrame
  };
}
