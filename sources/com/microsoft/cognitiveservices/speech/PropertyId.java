package com.microsoft.cognitiveservices.speech;

import com.xiaopeng.lib.framework.moduleinterface.appresourcemodule.IAppResourceException;
import com.xiaopeng.lib.utils.config.RemoteControlConfig;
/* loaded from: classes.dex */
public enum PropertyId {
    SpeechServiceConnection_Key(1000),
    SpeechServiceConnection_Endpoint(1001),
    SpeechServiceConnection_Region(1002),
    SpeechServiceAuthorization_Token(IAppResourceException.REASON_PARAM_ERROR),
    SpeechServiceAuthorization_Type(1004),
    SpeechServiceConnection_EndpointId(1005),
    SpeechServiceConnection_Host(1006),
    SpeechServiceConnection_ProxyHostName(1100),
    SpeechServiceConnection_ProxyPort(1101),
    SpeechServiceConnection_ProxyUserName(1102),
    SpeechServiceConnection_ProxyPassword(1103),
    SpeechServiceConnection_Url(1104),
    SpeechServiceConnection_TranslationToLanguages(RemoteControlConfig.SERVICE_TYPE_CARDIAGNOSIS_RUN_SH),
    SpeechServiceConnection_TranslationVoice(IAppResourceException.REASON_FILE_NOT_FOUND),
    SpeechServiceConnection_TranslationFeatures(IAppResourceException.REASON_MGR_DB_ERROR),
    SpeechServiceConnection_IntentRegion(2003),
    SpeechServiceConnection_RecoMode(3000),
    SpeechServiceConnection_RecoLanguage(IAppResourceException.REASON_NO_PERMISSION),
    Speech_SessionId(IAppResourceException.REASON_NOT_AVAILABLE_NOW),
    SpeechServiceConnection_RecoBackend(3004),
    SpeechServiceConnection_RecoModelName(3005),
    SpeechServiceConnection_RecoModelKey(3006),
    SpeechServiceConnection_SynthLanguage(3100),
    SpeechServiceConnection_SynthVoice(3101),
    SpeechServiceConnection_SynthOutputFormat(3102),
    SpeechServiceConnection_SynthEnableCompressedAudioTransmission(3103),
    SpeechServiceConnection_SynthBackend(3110),
    SpeechServiceConnection_SynthOfflineDataPath(3112),
    SpeechServiceConnection_SynthOfflineVoice(3113),
    SpeechServiceConnection_SynthModelKey(3114),
    SpeechServiceConnection_VoicesListEndpoint(3130),
    SpeechServiceConnection_InitialSilenceTimeoutMs(3200),
    SpeechServiceConnection_EndSilenceTimeoutMs(3201),
    SpeechServiceConnection_EnableAudioLogging(3202),
    SpeechServiceConnection_AtStartLanguageIdPriority(3203),
    SpeechServiceConnection_ContinuousLanguageIdPriority(3204),
    SpeechServiceResponse_RequestDetailedResultTrueFalse(4000),
    SpeechServiceResponse_RequestProfanityFilterTrueFalse(IAppResourceException.REASON_BINDER_FAILED),
    SpeechServiceResponse_ProfanityOption(IAppResourceException.REASON_BINDER_TIMEOUT),
    SpeechServiceResponse_PostProcessingOption(IAppResourceException.REASON_HTTP_NO_ETAG),
    SpeechServiceResponse_RequestWordLevelTimestamps(IAppResourceException.REASON_HTTP_NO_UPDATE),
    SpeechServiceResponse_StablePartialResultThreshold(IAppResourceException.REASON_HTTP_FILE_DOWNLOADING),
    SpeechServiceResponse_OutputFormatOption(IAppResourceException.REASON_HTTP_NOT_FOUND),
    SpeechServiceResponse_RequestSnr(IAppResourceException.REASON_HTTP_ERROR),
    SpeechServiceResponse_TranslationRequestStablePartialResult(4100),
    SpeechServiceResponse_RequestWordBoundary(4200),
    SpeechServiceResponse_RequestPunctuationBoundary(4201),
    SpeechServiceResponse_RequestSentenceBoundary(4202),
    SpeechServiceResponse_JsonResult(5000),
    SpeechServiceResponse_JsonErrorDetails(IAppResourceException.REASON_NOT_SUPPORT),
    SpeechServiceResponse_RecognitionLatencyMs(5002),
    SpeechServiceResponse_RecognitionBackend(5003),
    SpeechServiceResponse_SynthesisFirstByteLatencyMs(5010),
    SpeechServiceResponse_SynthesisFinishLatencyMs(5011),
    SpeechServiceResponse_SynthesisUnderrunTimeMs(5012),
    SpeechServiceResponse_SynthesisBackend(5020),
    CancellationDetails_Reason(6000),
    CancellationDetails_ReasonText(6001),
    CancellationDetails_ReasonDetailedText(6002),
    LanguageUnderstandingServiceResponse_JsonResult(7000),
    AudioConfig_DeviceNameForRender(8005),
    AudioConfig_PlaybackBufferLengthInMs(8006),
    AudioConfig_AudioProcessingOptions(8007),
    Speech_LogFilename(9001),
    Speech_SegmentationSilenceTimeoutMs(9002),
    Conversation_ApplicationId(10000),
    Conversation_DialogType(RemoteControlConfig.REMOTE_COMMAND_CONNECT_TOP_CAR_CAMERA_FEEDBACK),
    Conversation_Initial_Silence_Timeout(10002),
    Conversation_From_Id(10003),
    Conversation_Conversation_Id(10004),
    Conversation_Custom_Voice_Deployment_Ids(10005),
    Conversation_Speech_Activity_Template(10006),
    Conversation_Request_Bot_Status_Messages(10008),
    Conversation_Connection_Id(10009),
    SpeechServiceConnection_AutoDetectSourceLanguages(3300),
    SpeechServiceConnection_AutoDetectSourceLanguageResult(3301),
    DataBuffer_UserId(11002),
    DataBuffer_TimeStamp(11001),
    PronunciationAssessment_ReferenceText(12001),
    PronunciationAssessment_GradingSystem(12002),
    PronunciationAssessment_Granularity(12003),
    PronunciationAssessment_EnableMiscue(12005),
    PronunciationAssessment_PhonemeAlphabet(12006),
    PronunciationAssessment_NBestPhonemeCount(12007),
    PronunciationAssessment_Json(12009),
    PronunciationAssessment_Params(12010),
    SpeakerRecognition_Api_Version(13001);
    
    private final int id;

    PropertyId(int i) {
        this.id = i;
    }

    public int getValue() {
        return this.id;
    }
}
