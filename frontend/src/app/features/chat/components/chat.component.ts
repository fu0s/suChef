import { Component, OnInit, inject, ViewChild, ElementRef, AfterViewChecked, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { ChatService } from '../../../core/services/chat.service';
import { VoiceService } from '../../../core/services/voice.service';
import { Subscription } from 'rxjs';

export interface ChatMessage {
  id: string;
  content: string;
  sender: 'user' | 'ai';
  timestamp: Date;
  isStreaming?: boolean;
}

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateModule],
  templateUrl: './chat.component.html'
})
export class ChatComponent implements OnInit, AfterViewChecked, OnDestroy {
  messages: ChatMessage[] = [];
  messageInput: string = '';
  isLoading: boolean = false;
  isVoiceActive: boolean = false;
  isAutoSpeakEnabled: boolean = false;
  private voiceSubscription?: Subscription;

  @ViewChild('messagesContainer') private messagesContainer!: ElementRef;

  private translateService = inject(TranslateService);
  private chatService = inject(ChatService);
  private voiceService = inject(VoiceService);
  private shouldScrollToBottom = false;

  ngOnInit() {
    this.initializeChat();
    this.setupVoiceRecognition();
  }

  ngOnDestroy() {
    this.voiceSubscription?.unsubscribe();
  }

  ngAfterViewChecked() {
    if (this.shouldScrollToBottom) {
      this.scrollToBottom();
      this.shouldScrollToBottom = false;
    }
  }

  private scrollToBottom(): void {
    if (this.messagesContainer) {
      const container = this.messagesContainer.nativeElement;
      container.scrollTop = container.scrollHeight;
    }
  }

  initializeChat() {
    // Initial greeting from AI
    this.messages.push({
      id: '1',
      content: this.translateService.instant('chat.greeting') || 'Hello! I\'m SuChef AI. How can I help you today?',
      sender: 'ai',
      timestamp: new Date()
    });
  }

  setupVoiceRecognition() {
    this.voiceSubscription = this.voiceService.getSpeechResult().subscribe(result => {
      this.messageInput = result.transcript;
      if (result.isFinal) {
        this.sendMessage();
      }
    });

    // Subscribe to listening state to keep UI in sync
    this.voiceSubscription.add(
      this.voiceService.isListening$.subscribe(isListening => {
        this.isVoiceActive = isListening;

        // If listening stopped and we have pending input that wasn't sent as final
        // It means the recognition timed out or stopped naturally while speaking
        if (!isListening && this.messageInput.trim() && !this.isLoading) {
          this.sendMessage();
        }
      })
    );
  }

  toggleVoice() {
    // Unlock audio on generic user click to bypass autoplay
    if (this.isAutoSpeakEnabled) {
      this.voiceService.unlockAudio();
    }

    if (this.isVoiceActive) {
      this.voiceService.stopListening();
    } else {
      this.voiceService.startListening();
    }
  }

  toggleAutoSpeak() {
    this.isAutoSpeakEnabled = !this.isAutoSpeakEnabled;
    if (!this.isAutoSpeakEnabled) {
      window.speechSynthesis.cancel();
    } else {
      // Unlock audio when enabling
      this.voiceService.unlockAudio();
    }
  }

  sendMessage() {
    if (!this.messageInput.trim() || this.isLoading) return;

    // Add user message
    const userMessage: ChatMessage = {
      id: Date.now().toString(),
      content: this.messageInput,
      sender: 'user',
      timestamp: new Date()
    };
    this.messages.push(userMessage);
    const userInput = this.messageInput;
    this.messageInput = '';
    this.isLoading = true;
    this.shouldScrollToBottom = true;

    // Create AI message placeholder
    const aiMessage: ChatMessage = {
      id: (Date.now() + 1).toString(),
      content: '',
      sender: 'ai',
      timestamp: new Date()
    };
    this.messages.push(aiMessage);

    // Call backend AI (non-streaming endpoint that supports tool calling)
    this.chatService.sendMessage(userInput).subscribe({
      next: (response) => {
        aiMessage.content = response;
        this.shouldScrollToBottom = true;
      },
      error: (error) => {
        console.error('Error calling AI API:', error);
        aiMessage.content = this.translateService.instant('chat.response.error') || 'Sorry, an error occurred. Please try again.';
        this.isLoading = false;
      },
      complete: () => {
        this.isLoading = false;
        this.shouldScrollToBottom = true;
        if (this.isAutoSpeakEnabled) {
          this.voiceService.speak(aiMessage.content);
        }
      }
    });
  }

  /**
   * Simple formatter for AI messages to support bullet points and basic tables
   */
  formatMessage(content: string): string {
    if (!content) return '';

    let formatted = content;

    // Handle bullet points (lines starting with * or - followed by space)
    formatted = formatted.replace(/^[\s]*[\*\-][\s]+(.*)$/gm, '<li>$1</li>');
    formatted = formatted.replace(/(<li>.*<\/li>)/gms, '<ul>$1</ul>');
    // Basic cleanup for nested lists if any (though this parser is simple)
    formatted = formatted.replace(/<\/ul>[\s]*<ul>/g, '');

    // Handle Tables
    // Pattern: | col1 | col2 | ... |
    //          | ---  | ---  | ... |
    //          | val1 | val2 | ... |
    const lines = formatted.split('\n');
    let inTable = false;
    let tableHtml = '';
    const outputLines: string[] = [];

    for (let i = 0; i < lines.length; i++) {
      const line = lines[i].trim();
      if (line.startsWith('|') && line.endsWith('|')) {
        if (!inTable) {
          inTable = true;
          tableHtml = '<table><thead>';
          // Header row
          const cols = line.split('|').filter(c => c.trim().length > 0 || line.includes('||')); // Keep empty middle cols if needed
          tableHtml += '<tr>' + cols.map(c => `<th>${c.trim()}</th>`).join('') + '</tr></thead><tbody>';
          
          // Skip the separator row if it's there
          if (i + 1 < lines.length && lines[i+1].trim().includes('---')) {
            i++;
          }
        } else {
          const cols = line.split('|').filter(c => c.trim().length > 0 || line.includes('||'));
          tableHtml += '<tr>' + cols.map(c => `<td>${c.trim()}</td>`).join('') + '</tr>';
        }
      } else {
        if (inTable) {
          tableHtml += '</tbody></table>';
          outputLines.push(tableHtml);
          inTable = false;
          tableHtml = '';
        }
        outputLines.push(line);
      }
    }
    
    if (inTable) {
      tableHtml += '</tbody></table>';
      outputLines.push(tableHtml);
    }

    formatted = outputLines.join('\n');

    // Handle line breaks (preserving existing ones that aren't part of special blocks)
    // Avoid double BRs after list/table end
    formatted = formatted.replace(/\n(?!<table|<ul|<li|<\/table|<\/ul|<\/li)/g, '<br>');

    return formatted;
  }
}