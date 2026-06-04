import { Component, OnInit, OnDestroy, ViewChild, ElementRef, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { gsap } from 'gsap';
import { ScrollTrigger } from 'gsap/ScrollTrigger';
import * as THREE from 'three';

gsap.registerPlugin(ScrollTrigger);

@Component({
  selector: 'app-ai-section',
  standalone: true,
  imports: [CommonModule, TranslateModule],
  templateUrl: './ai-section.component.html',
  styleUrls: ['./ai-section.component.scss']
})
export class AiSectionComponent implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('canvasContainer', { static: false }) canvasContainer!: ElementRef;
  isMobile: boolean = false;

  private scene!: THREE.Scene;
  private camera!: THREE.PerspectiveCamera;
  private renderer!: THREE.WebGLRenderer;
  private group!: THREE.Group;
  private animationFrameId: number | null = null;
  private targetRotation = 0;

  ngOnInit(): void {
    this.checkIsMobile();
    window.addEventListener('resize', this.onResize);
  }

  ngAfterViewInit(): void {
    this.initScrollReveal();

    if (!this.isMobile && this.canvasContainer) {
      setTimeout(() => {
        this.initThreeJs();
      }, 100);
    }
  }

  private checkIsMobile(): void {
    this.isMobile = window.innerWidth < 768;
  }

  private initThreeJs(): void {
    if (!this.canvasContainer) return;
    const container = this.canvasContainer.nativeElement;
    
    try {
      this.scene = new THREE.Scene();
      this.scene.background = null;

      this.camera = new THREE.PerspectiveCamera(60, container.clientWidth / container.clientHeight, 0.1, 1000);
      this.camera.position.z = 4;

      this.renderer = new THREE.WebGLRenderer({ alpha: true, antialias: true });
      this.renderer.setSize(container.clientWidth, container.clientHeight);
      this.renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
      
      container.innerHTML = '';
      container.appendChild(this.renderer.domElement);

    this.group = new THREE.Group();
    this.scene.add(this.group);

    const particlesCount = 80;
    const geometry = new THREE.BufferGeometry();
    const positions = new Float32Array(particlesCount * 3);
    const pointCloud: THREE.Vector3[] = [];

    for (let i = 0; i < particlesCount; i++) {
        const x = (Math.random() - 0.5) * 6;
        const y = (Math.random() - 0.5) * 6;
        const z = (Math.random() - 0.5) * 6;
        
        positions[i * 3] = x;
        positions[i * 3 + 1] = y;
        positions[i * 3 + 2] = z;
        
        pointCloud.push(new THREE.Vector3(x, y, z));
    }
    
    geometry.setAttribute('position', new THREE.BufferAttribute(positions, 3));

    const material = new THREE.PointsMaterial({
        color: 0x98FF98,
        size: 0.08,
        transparent: true,
        opacity: 0.8
    });

    const particles = new THREE.Points(geometry, material);
    this.group.add(particles);

    // Add lines to simulate neural network
    const lineMaterial = new THREE.LineBasicMaterial({
        color: 0x98FF98,
        transparent: true,
        opacity: 0.15
    });

    const lineGeometry = new THREE.BufferGeometry();
    const linePositions = [];
    
    for (let i = 0; i < particlesCount; i++) {
        for (let j = i + 1; j < particlesCount; j++) {
            const dist = pointCloud[i].distanceTo(pointCloud[j]);
            if (dist < 1.5) {
                linePositions.push(
                    pointCloud[i].x, pointCloud[i].y, pointCloud[i].z,
                    pointCloud[j].x, pointCloud[j].y, pointCloud[j].z
                );
            }
        }
    }
    
    lineGeometry.setAttribute('position', new THREE.Float32BufferAttribute(linePositions, 3));
    const lines = new THREE.LineSegments(lineGeometry, lineMaterial);
    this.group.add(lines);

      const ambientLight = new THREE.AmbientLight(0xffffff, 0.5);
      this.scene.add(ambientLight);

      this.animate();
    } catch (error) {
      console.warn('Failed to initialize THREE.js for AI section:', error);
      // Component will still display with CSS animations
    }
  }

  private initScrollReveal(): void {
    // Animate stats in on scroll — no pinning, which was causing the section
    // to bleed into the hero's pinned scroll zone
    gsap.utils.toArray('[data-ai-stat]').forEach((stat: any, i) => {
      gsap.fromTo(stat,
        { opacity: 0, y: 30 },
        {
          opacity: 1,
          y: 0,
          duration: 0.6,
          delay: i * 0.1,
          scrollTrigger: {
            trigger: '[data-ai-section]',
            start: 'top 80%',
            once: true
          }
        }
      );
    });

    // Animate the header & subtitle
    gsap.fromTo('[data-ai-section] .ai-header',
      { opacity: 0, y: 40 },
      {
        opacity: 1,
        y: 0,
        duration: 0.8,
        scrollTrigger: {
          trigger: '[data-ai-section]',
          start: 'top 80%',
          once: true
        }
      }
    );
  }

  private updateAiVisualization(progress: number): void {
    this.targetRotation = progress * Math.PI * 2;
  }

  private onResize = (): void => {
    this.checkIsMobile();
    if (this.camera && this.renderer) {
      this.camera.aspect = window.innerWidth / window.innerHeight;
      this.camera.updateProjectionMatrix();
      this.renderer.setSize(window.innerWidth, window.innerHeight);
    }
  }

  private animate = (): void => {
    if (this.group) {
        // Continuous slow rotation
        this.group.rotation.y += 0.001;
        this.group.rotation.x += 0.0005;

        // Apply scroll-based rotation
        this.group.rotation.y += (this.targetRotation - this.group.rotation.y) * 0.05;
    }

    if (this.renderer && this.scene && this.camera) {
        this.renderer.render(this.scene, this.camera);
    }
    this.animationFrameId = requestAnimationFrame(this.animate);
  }

  ngOnDestroy(): void {
    window.removeEventListener('resize', this.onResize);
    ScrollTrigger.getAll().forEach(trigger => trigger.kill());
    
    if (this.animationFrameId !== null) {
      cancelAnimationFrame(this.animationFrameId);
    }
    if (this.renderer) {
      this.renderer.dispose();
    }
  }
}
