import { Injectable, NgZone } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ChatService {
  private streamUrl = `${environment.apiUrl}/api/ai/stream`;
  private chatUrl = `${environment.apiUrl}/api/ai/chat`;

  constructor(private ngZone: NgZone) { }

  /**
   * Sends a message to the AI and returns the full response at once.
   * Uses the synchronous /api/ai/chat endpoint which supports tool calling.
   * @param message The user's message.
   * @returns An Observable<string> that emits the complete response.
   */
  sendMessage(message: string): Observable<string> {
    return new Observable<string>(observer => {
      const url = `${this.chatUrl}?message=${encodeURIComponent(message)}`;

      fetch(url, {
        method: 'GET',
        credentials: 'include'
      }).then(response => {
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.text();
      }).then(text => {
        this.ngZone.run(() => {
          observer.next(text);
          observer.complete();
        });
      }).catch(error => {
        this.ngZone.run(() => observer.error(error));
      });
    });
  }

  /**
   * Sends a message to the AI and returns an Observable that emits each token as it arrives.
   * Uses fetch with ReadableStream to handle Server-Sent Events.
   * @param message The user's message.
   * @returns An Observable<string> that emits partial response tokens.
   */
  sendMessageStream(message: string): Observable<string> {
    return new Observable<string>(observer => {
      const url = `${this.streamUrl}?message=${encodeURIComponent(message)}`;

      const headers: HeadersInit = {
        'Accept': 'text/event-stream'
      };

      // JWT is stored in an HttpOnly cookie — browser sends it automatically with credentials: 'include'
      fetch(url, {
        method: 'GET',
        headers: headers,
        credentials: 'include'
      }).then(response => {
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        const reader = response.body?.getReader();
        if (!reader) {
          throw new Error('ReadableStream not supported');
        }
        const decoder = new TextDecoder();

        let buffer = '';
        const read = (): void => {
          reader.read().then(({ done, value }) => {
            if (done) {
              if (buffer) {
                this.processEvent(buffer, observer);
              }
              this.ngZone.run(() => observer.complete());
              return;
            }

            buffer += decoder.decode(value, { stream: true });

            // SSE events are separated by double newlines
            const events = buffer.split('\n\n');

            // Keep the last partial event in the buffer
            buffer = events.pop() || '';

            for (const event of events) {
              this.processEvent(event, observer);
            }
            read();
          }).catch(error => {
            this.ngZone.run(() => observer.error(error));
          });
        };
        read();
      }).catch(error => {
        this.ngZone.run(() => observer.error(error));
      });

      // Return cleanup function
      return () => {
        // Cleanup if needed
      };
    });
  }

  /**
   * Processes a single SSE event block.
   * According to SSE spec, multiple 'data:' lines in one event should be joined by a newline.
   */
  private processEvent(event: string, observer: any): void {
    const lines = event.split('\n');
    let data = '';

    for (const line of lines) {
      if (line.startsWith('data:')) {
        // We take everything after 'data:'. 
        // We DO NOT strip the leading space because in LLM streaming, 
        // that space is often the only thing separating tokens.
        const content = line.substring(5);
        data += (data ? '\n' : '') + content;
      }
    }

    if (data !== '') {
      this.ngZone.run(() => observer.next(data));
    }
  }
}
