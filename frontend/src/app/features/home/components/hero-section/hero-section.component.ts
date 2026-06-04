import { Component, OnInit, OnDestroy, ViewChild, ElementRef, AfterViewInit, HostListener, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { gsap } from 'gsap';
import { ScrollTrigger } from 'gsap/ScrollTrigger';
import * as THREE from 'three';

gsap.registerPlugin(ScrollTrigger);

@Component({
  selector: 'app-hero-section',
  standalone: true,
  imports: [CommonModule, TranslateModule],
  templateUrl: './hero-section.component.html',
  styleUrls: ['./hero-section.component.scss']
})
export class HeroSectionComponent implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('canvasContainer', { static: false }) canvasContainer!: ElementRef;

  isLoading = true;
  isFadingOut = false;
  loadingProgress = 0;

  private scene!: THREE.Scene;
  private camera!: THREE.PerspectiveCamera;
  private renderer!: THREE.WebGLRenderer;
  private planeMesh!: THREE.Mesh<THREE.PlaneGeometry, THREE.MeshBasicMaterial>;

  private animationFrameId: number | null = null;
  private textures: THREE.Texture[] = [];
  private totalFrames = 192;

  private currentFrameIndex = 0;
  private targetFrameIndex = 0;

  constructor(private cdr: ChangeDetectorRef) { }

  ngOnInit(): void {
  }

  ngAfterViewInit(): void {
    this.loadTextures();
  }

  private loadTextures(): void {
    const manager = new THREE.LoadingManager();
    const loader = new THREE.TextureLoader(manager);

    const threshold = Math.floor(this.totalFrames * 0.5);
    let initTriggered = false;

    manager.onProgress = (url, itemsLoaded, itemsTotal) => {
      this.loadingProgress = (itemsLoaded / this.totalFrames) * 100;
      this.cdr.detectChanges();

      if (itemsLoaded >= threshold && !initTriggered) {
        initTriggered = true;
        this.completeLoading();
      }
    };

    manager.onLoad = () => {
      if (!initTriggered) {
        initTriggered = true;
        this.loadingProgress = 100;
        this.cdr.detectChanges();
        this.completeLoading();
      }
    };

    for (let i = 1; i <= this.totalFrames; i++) {
      const frameNumber = i.toString().padStart(3, '0');
      const url = `/assets/images/landing/animation/frame_0${frameNumber}.jpg`;
      loader.load(url, (texture) => {
        texture.minFilter = THREE.LinearFilter;
        texture.magFilter = THREE.LinearFilter;
        texture.colorSpace = THREE.SRGBColorSpace;
        this.textures[i - 1] = texture;
      });
    }
  }

  private completeLoading(): void {
    this.isFadingOut = true;
    this.cdr.detectChanges();

    setTimeout(() => {
      this.isLoading = false;
      this.isFadingOut = false;
      this.cdr.detectChanges();

      this.initThreeJs();
      this.initHeroAnimation();
    }, 1000);
  }

  private initHeroAnimation(): void {
    const headline = document.querySelector('[data-hero-headline]') as HTMLElement;
    const buttons = document.querySelectorAll('[data-hero-button]');

    if (headline) {
      gsap.to(headline, {
        opacity: 1,
        y: 0,
        duration: 0.8,
        delay: 0.2
      });
    }

    buttons.forEach((btn, index) => {
      gsap.to(btn, {
        opacity: 1,
        y: 0,
        duration: 0.6,
        delay: 0.6 + 0.2 * index
      });
    });

    ScrollTrigger.create({
      trigger: '#hero-section',
      start: 'top top',
      end: '+=200%',
      pin: true,
      onUpdate: (self) => {
        this.targetFrameIndex = self.progress * (this.totalFrames - 1);
      }
    });
  }

  private initThreeJs(): void {
    if (!this.canvasContainer) return;

    const container = this.canvasContainer.nativeElement;

    this.scene = new THREE.Scene();

    const aspect = window.innerWidth / window.innerHeight;
    this.camera = new THREE.PerspectiveCamera(75, aspect, 0.1, 1000);
    this.camera.position.z = 5;

    this.renderer = new THREE.WebGLRenderer({ alpha: true, antialias: true, powerPreference: "high-performance" });
    this.renderer.setSize(window.innerWidth, window.innerHeight);
    this.renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));

    container.innerHTML = '';
    container.appendChild(this.renderer.domElement);

    const firstTexture = this.textures[0];
    const geometry = new THREE.PlaneGeometry(1, 1);
    const material = new THREE.MeshBasicMaterial({
      map: firstTexture,
      depthTest: false,
      depthWrite: false
    });

    this.planeMesh = new THREE.Mesh(geometry, material);
    this.scene.add(this.planeMesh);

    this.updatePlaneSize();
    this.animate();
  }

  private updatePlaneSize(): void {
    if (!this.camera || !this.planeMesh || !this.textures[0]) return;

    const dist = this.camera.position.z;
    const vFov = THREE.MathUtils.degToRad(this.camera.fov);
    const height = 2 * Math.tan(vFov / 2) * dist;
    const width = height * this.camera.aspect;

    const image: any = this.textures[0].image;
    let imageAspect = 16 / 9;
    if (image && image.width && image.height) {
      imageAspect = image.width / image.height;
    }

    const viewportAspect = window.innerWidth / window.innerHeight;

    let scaleX = width;
    let scaleY = height;

    if (viewportAspect > imageAspect) {
      scaleY = width / imageAspect;
    } else {
      scaleX = height * imageAspect;
    }

    const cropFactor = 1.15; // Zoom in to crop out watermark
    this.planeMesh.scale.set(scaleX * cropFactor, scaleY * cropFactor, 1);
  }

  @HostListener('window:resize')
  onResize(): void {
    if (!this.camera || !this.renderer) return;

    this.camera.aspect = window.innerWidth / window.innerHeight;
    this.camera.updateProjectionMatrix();
    this.renderer.setSize(window.innerWidth, window.innerHeight);
    this.updatePlaneSize();
  }

  private animate = (): void => {
    // Increased interpolation factor for faster, smoother responsiveness
    this.currentFrameIndex += (this.targetFrameIndex - this.currentFrameIndex) * 0.15;

    const indexToRender = Math.min(Math.max(Math.round(this.currentFrameIndex), 0), this.totalFrames - 1);

    if (this.planeMesh && this.textures[indexToRender]) {
      this.planeMesh.material.map = this.textures[indexToRender];
      this.planeMesh.material.needsUpdate = true;
    }

    if (this.renderer && this.scene && this.camera) {
      this.renderer.render(this.scene, this.camera);
    }

    this.animationFrameId = requestAnimationFrame(this.animate);
  }

  ngOnDestroy(): void {
    ScrollTrigger.getAll().forEach(trigger => trigger.kill());
    if (this.animationFrameId !== null) {
      cancelAnimationFrame(this.animationFrameId);
    }
    if (this.renderer) {
      this.renderer.dispose();
    }
    if (this.planeMesh) {
      this.planeMesh.geometry.dispose();
      this.planeMesh.material.dispose();
    }
    this.textures.forEach(t => {
      if (t) t.dispose();
    });
  }
}
