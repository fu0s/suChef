import { Injectable, NgZone } from '@angular/core';
import { Subject, Observable } from 'rxjs';
import { TranslationService } from './translation.service';

@Injectable({
    providedIn: 'root'
})
export class VoiceService {
    private recognition: any;
    private isListening = false;
    private speechResultSubject = new Subject<{ transcript: string, isFinal: boolean }>();
    private isListeningSubject = new Subject<boolean>();

    // Expose observables
    public isListening$ = this.isListeningSubject.asObservable();

    constructor(private ngZone: NgZone, private translationService: TranslationService) {
        this.initRecognition();
    }

    private getSpeechLanguage(): string {
        const lang = this.translationService.getLanguage();
        return lang === 'de' ? 'de-DE' : 'en-US';
    }

    private initRecognition() {
        const SpeechRecognition = (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition;
        if (SpeechRecognition) {
            this.recognition = new SpeechRecognition();
            this.recognition.continuous = false;
            // Enable interim results for real-time transcription
            this.recognition.interimResults = true;
            this.recognition.lang = this.getSpeechLanguage();

            this.recognition.onstart = () => {
                this.ngZone.run(() => {
                    this.isListening = true;
                    this.isListeningSubject.next(true);
                });
            };

            this.recognition.onresult = (event: any) => {
                let interimTranscript = '';
                let finalTranscript = '';

                for (let i = event.resultIndex; i < event.results.length; ++i) {
                    if (event.results[i].isFinal) {
                        finalTranscript += event.results[i][0].transcript;
                    } else {
                        interimTranscript += event.results[i][0].transcript;
                    }
                }

                this.ngZone.run(() => {
                    if (finalTranscript) {
                        this.speechResultSubject.next({ transcript: finalTranscript, isFinal: true });
                    } else if (interimTranscript) {
                        this.speechResultSubject.next({ transcript: interimTranscript, isFinal: false });
                    }
                });
            };

            this.recognition.onend = () => {
                this.ngZone.run(() => {
                    this.isListening = false;
                    this.isListeningSubject.next(false);
                });
            };

            this.recognition.onerror = (event: any) => {
                console.error('Speech recognition error:', event.error);
                this.ngZone.run(() => {
                    this.isListening = false;
                    this.isListeningSubject.next(false);
                });
            };
        } else {
            console.warn('Speech Recognition API not supported in this browser.');
        }
    }

    startListening() {
        if (this.recognition && !this.isListening) {
            try {
                this.recognition.lang = this.getSpeechLanguage();
                this.recognition.start();
            } catch (e) {
                console.error('Error starting recognition:', e);
            }
        }
    }

    stopListening() {
        if (this.recognition && this.isListening) {
            this.recognition.stop();
        }
    }

    getSpeechResult(): Observable<{ transcript: string, isFinal: boolean }> {
        return this.speechResultSubject.asObservable();
    }

    getIsListening(): boolean {
        return this.isListening;
    }

    // Bypass browser autoplay restrictions by triggering speech on user click
    unlockAudio() {
        if ('speechSynthesis' in window) {
            const utterance = new SpeechSynthesisUtterance('');
            window.speechSynthesis.speak(utterance);
        }
    }

    speak(text: string) {
        if ('speechSynthesis' in window) {
            // Cancel any ongoing speech
            window.speechSynthesis.cancel();

            const utterance = new SpeechSynthesisUtterance(text);
            utterance.lang = this.getSpeechLanguage();
            utterance.rate = 1.0;
            utterance.pitch = 1.0;
            window.speechSynthesis.speak(utterance);
        } else {
            console.warn('Speech Synthesis API not supported in this browser.');
        }
    }
}
