declare module 'aos' {
  interface AOSOptions {
    duration?: number;
    easing?: string;
    delay?: number | Function;
    offset?: number;
    once?: boolean;
    mirror?: boolean;
    animatedClassName?: string;
    disableMutationObserver?: boolean;
    throttleDelay?: number;
    startEvent?: string;
    initClassName?: string;
    useClassNames?: boolean;
    debounceDelay?: number;
    skipAutoinit?: boolean;
    anchorPlacement?: string;
  }

  interface AOS {
    init(options?: AOSOptions): void;
    refresh(): void;
    refreshHard(): void;
  }

  const aos: AOS;
  export default aos;
}
