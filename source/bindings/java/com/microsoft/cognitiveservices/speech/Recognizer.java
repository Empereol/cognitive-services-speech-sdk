package com.microsoft.cognitiveservices.speech;
//
// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE.md file in the project root for full license information.
//

import java.io.Closeable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.microsoft.cognitiveservices.speech.audio.AudioConfig;
import com.microsoft.cognitiveservices.speech.util.EventHandlerImpl;
import com.microsoft.cognitiveservices.speech.util.Contracts;

/**
 * Defines the base class Recognizer which mainly contains common event handlers.
 */
public class Recognizer implements Closeable
{
    protected static ExecutorService s_executorService;
    static {
        s_executorService = Executors.newCachedThreadPool();
    }

    /**
     * Defines event handler for session started event.
     */
    final public EventHandlerImpl<SessionEventArgs> sessionStarted = new EventHandlerImpl<SessionEventArgs>();

    /**
     * Defines event handler for session stopped event.
     */
    final public EventHandlerImpl<SessionEventArgs> sessionStopped = new EventHandlerImpl<SessionEventArgs>();

    /**
     * Defines event handler for speech start detected event.
     */
    final public EventHandlerImpl<RecognitionEventArgs> speechStartDetected = new EventHandlerImpl<RecognitionEventArgs>();

    /**
     * Defines event handler for speech end detected event.
     */
    final public EventHandlerImpl<RecognitionEventArgs> speechEndDetected = new EventHandlerImpl<RecognitionEventArgs>();

    private AudioConfig audioInputKeepAlive;

    /**
     * Creates and initializes an instance of a Recognizer
     * @param audioInput An optional audio input configuration associated with the recognizer
     */
    protected Recognizer(AudioConfig audioInput) {
        // Note: Since ais is optional, no test for null reference
        audioInputKeepAlive = audioInput;
        sessionStartedHandler = new SessionEventHandlerImpl(this, SessionEventType.SessionStartedEvent);
        sessionStoppedHandler = new SessionEventHandlerImpl(this, SessionEventType.SessionStoppedEvent);
        speechStartDetectedHandler = new RecognitionEventHandlerImpl(this, RecognitionEventType.SpeechStartDetectedEvent);
        speechEndDetectedHandler = new RecognitionEventHandlerImpl(this, RecognitionEventType.SpeechEndDetectedEvent);
    }

    /**
     * Dispose of associated resources.
     */
    @Override
    public void close() {
        dispose(true);
    }

    /**
     * This method performs cleanup of resources.
     * The Boolean parameter disposing indicates whether the method is called from Dispose (if disposing is true) or from the finalizer (if disposing is false).
     * Derived classes should override this method to dispose resource if needed.
     * @param disposing Flag to request disposal.
     */
    protected void dispose(boolean disposing) {
        if (disposed) {
            return;
        }

        if (disposing) {
            // disconnect
            sessionStartedHandler.delete();
            sessionStoppedHandler.delete();
            speechStartDetectedHandler.delete();
            speechEndDetectedHandler.delete();
        }

        disposed = true;
    }

    protected SessionEventHandlerImpl sessionStartedHandler;
    protected SessionEventHandlerImpl sessionStoppedHandler;
    protected RecognitionEventHandlerImpl speechStartDetectedHandler;
    protected RecognitionEventHandlerImpl speechEndDetectedHandler;
    private boolean disposed = false;

    /**
     * Define an internal class which raise an event when a corresponding callback is invoked from the native layer.
     */
    class SessionEventHandlerImpl extends com.microsoft.cognitiveservices.speech.internal.SessionEventListener {

        public SessionEventHandlerImpl(Recognizer recognizer, SessionEventType eventType) {
            Contracts.throwIfNull(recognizer, "recognizer");

            this.recognizer = recognizer;
            this.eventType = eventType;
        }

        @Override
        public void Execute(com.microsoft.cognitiveservices.speech.internal.SessionEventArgs eventArgs) {
            Contracts.throwIfNull(eventArgs, "eventArgs");

            if (recognizer.disposed) {
                return;
            }

            SessionEventArgs arg = new SessionEventArgs(eventArgs);
            EventHandlerImpl<SessionEventArgs>  handler = this.eventType == SessionEventType.SessionStartedEvent ?
                    this.recognizer.sessionStarted : this.recognizer.sessionStopped;

            if (handler != null) {
                handler.fireEvent(this.recognizer, arg);
            }
        }

        private Recognizer recognizer;
        private SessionEventType eventType;

    }

    /**
     * Define an internal class which raise an event when a corresponding callback is invoked from the native layer.
     */
    class RecognitionEventHandlerImpl extends com.microsoft.cognitiveservices.speech.internal.RecognitionEventListener
    {
        public RecognitionEventHandlerImpl(Recognizer recognizer, RecognitionEventType eventType)
        {
            Contracts.throwIfNull(recognizer, "recognizer");

            this.recognizer = recognizer;
            this.eventType = eventType;
        }

        @Override
        public void Execute(com.microsoft.cognitiveservices.speech.internal.RecognitionEventArgs eventArgs)
        {
            Contracts.throwIfNull(eventArgs, "eventArgs");

            if (recognizer.disposed)
            {
                return;
            }

            RecognitionEventArgs arg = new RecognitionEventArgs(eventArgs);
            EventHandlerImpl<RecognitionEventArgs>  handler = this.eventType == RecognitionEventType.SpeechStartDetectedEvent ?
                    this.recognizer.speechStartDetected : this.recognizer.speechEndDetected;

            if (handler != null)
            {
                handler.fireEvent(this.recognizer, arg);
            }
        }

        private Recognizer recognizer;
        private RecognitionEventType eventType;

    }
}
