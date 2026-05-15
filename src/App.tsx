import { useEffect, useRef, useState } from 'react'
import gsap from 'gsap'
import { ScrollTrigger } from 'gsap/ScrollTrigger'
import Lenis from '@studio-freight/lenis'
import './App.css'

gsap.registerPlugin(ScrollTrigger)

/* ─── Confetti Canvas ─── */
function ConfettiCanvas() {
  const canvasRef = useRef<HTMLCanvasElement>(null)

  useEffect(() => {
    const canvas = canvasRef.current
    if (!canvas) return
    const ctx = canvas.getContext('2d')
    if (!ctx) return

    canvas.width = window.innerWidth
    canvas.height = window.innerHeight

    const colors = ['#C9A84C', '#FFD700', '#FFA500', '#FF6B6B', '#E8C96A', '#F5F0E8', '#ffffff']
    const particles: {
      x: number; y: number; vx: number; vy: number; size: number
      color: string; rotation: number; rotationSpeed: number; opacity: number
    }[] = []

    // Create particles
    for (let i = 0; i < 150; i++) {
      particles.push({
        x: Math.random() * canvas.width,
        y: Math.random() * canvas.height - canvas.height,
        vx: (Math.random() - 0.5) * 2,
        vy: Math.random() * 1.5 + 0.5,
        size: Math.random() * 6 + 3,
        color: colors[Math.floor(Math.random() * colors.length)],
        rotation: Math.random() * Math.PI * 2,
        rotationSpeed: (Math.random() - 0.5) * 0.1,
        opacity: Math.random() * 0.6 + 0.2,
      })
    }

    let animId: number
    const animate = () => {
      ctx.clearRect(0, 0, canvas.width, canvas.height)
      particles.forEach(p => {
        p.x += p.vx
        p.y += p.vy
        p.rotation += p.rotationSpeed
        if (p.y > canvas.height + 20) {
          p.y = -20
          p.x = Math.random() * canvas.width
        }
        if (p.x < -20) p.x = canvas.width + 20
        if (p.x > canvas.width + 20) p.x = -20

        ctx.save()
        ctx.translate(p.x, p.y)
        ctx.rotate(p.rotation)
        ctx.globalAlpha = p.opacity
        ctx.fillStyle = p.color
        // Draw confetti rectangle
        ctx.fillRect(-p.size / 2, -p.size / 2, p.size, p.size * 0.6)
        ctx.restore()
      })
      animId = requestAnimationFrame(animate)
    }
    animate()

    const handleResize = () => {
      canvas.width = window.innerWidth
      canvas.height = window.innerHeight
    }
    window.addEventListener('resize', handleResize)
    return () => {
      cancelAnimationFrame(animId)
      window.removeEventListener('resize', handleResize)
    }
  }, [])

  return (
    <canvas
      ref={canvasRef}
      style={{
        position: 'fixed',
        top: 0,
        left: 0,
        width: '100%',
        height: '100%',
        pointerEvents: 'none',
        zIndex: 1,
      }}
    />
  )
}

/* ─── Particle Background ─── */
function ParticleBackground() {
  const canvasRef = useRef<HTMLCanvasElement>(null)

  useEffect(() => {
    const canvas = canvasRef.current
    if (!canvas) return
    const ctx = canvas.getContext('2d')
    if (!ctx) return

    canvas.width = window.innerWidth
    canvas.height = window.innerHeight

    const particles: { x: number; y: number; vx: number; vy: number; size: number; opacity: number }[] = []
    for (let i = 0; i < 80; i++) {
      particles.push({
        x: Math.random() * canvas.width,
        y: Math.random() * canvas.height,
        vx: (Math.random() - 0.5) * 0.3,
        vy: (Math.random() - 0.5) * 0.3,
        size: Math.random() * 2 + 0.5,
        opacity: Math.random() * 0.4 + 0.1,
      })
    }

    let animId: number
    const animate = () => {
      ctx.clearRect(0, 0, canvas.width, canvas.height)
      particles.forEach(p => {
        p.x += p.vx
        p.y += p.vy
        if (p.x < 0) p.x = canvas.width
        if (p.x > canvas.width) p.x = 0
        if (p.y < 0) p.y = canvas.height
        if (p.y > canvas.height) p.y = 0

        ctx.beginPath()
        ctx.arc(p.x, p.y, p.size, 0, Math.PI * 2)
        ctx.fillStyle = `rgba(201, 168, 76, ${p.opacity})`
        ctx.fill()
      })

      // Draw connection lines
      for (let i = 0; i < particles.length; i++) {
        for (let j = i + 1; j < particles.length; j++) {
          const dx = particles[i].x - particles[j].x
          const dy = particles[i].y - particles[j].y
          const dist = Math.sqrt(dx * dx + dy * dy)
          if (dist < 120) {
            ctx.beginPath()
            ctx.moveTo(particles[i].x, particles[i].y)
            ctx.lineTo(particles[j].x, particles[j].y)
            ctx.strokeStyle = `rgba(201, 168, 76, ${0.08 * (1 - dist / 120)})`
            ctx.lineWidth = 0.5
            ctx.stroke()
          }
        }
      }
      animId = requestAnimationFrame(animate)
    }
    animate()

    const handleResize = () => {
      canvas.width = window.innerWidth
      canvas.height = window.innerHeight
    }
    window.addEventListener('resize', handleResize)
    return () => {
      cancelAnimationFrame(animId)
      window.removeEventListener('resize', handleResize)
    }
  }, [])

  return (
    <canvas
      ref={canvasRef}
      style={{
        position: 'fixed',
        top: 0,
        left: 0,
        width: '100%',
        height: '100%',
        pointerEvents: 'none',
        zIndex: 0,
      }}
    />
  )
}

/* ─── Navigation ─── */
function Navigation() {
  const navRef = useRef<HTMLElement>(null)

  useEffect(() => {
    const nav = navRef.current
    if (!nav) return

    const handleScroll = () => {
      if (window.scrollY > 100) {
        nav.style.background = 'rgba(11, 13, 23, 0.85)'
        nav.style.backdropFilter = 'blur(12px)'
      } else {
        nav.style.background = 'transparent'
        nav.style.backdropFilter = 'none'
      }
    }
    window.addEventListener('scroll', handleScroll)
    return () => window.removeEventListener('scroll', handleScroll)
  }, [])

  return (
    <nav
      ref={navRef}
      className="fixed top-0 left-0 w-full z-50 transition-all duration-500"
      style={{ height: 64 }}
    >
      <div className="max-w-[1400px] mx-auto px-6 h-full flex items-center justify-between">
        <span className="font-mono text-xs tracking-[0.2em] text-[#C9A84C]">
          PRANSH
        </span>
        <div className="flex items-center gap-6">
          <a href="#gallery" className="text-xs font-mono tracking-wider text-[#F5F0E8]/60 hover:text-[#C9A84C] transition-colors">
            GALLERY
          </a>
          <a href="#message" className="text-xs font-mono tracking-wider text-[#F5F0E8]/60 hover:text-[#C9A84C] transition-colors">
            MESSAGE
          </a>
          <a
            href="https://www.instagram.com/ibipranshgoswami"
            target="_blank"
            rel="noopener noreferrer"
            className="text-xs font-mono tracking-wider text-[#C9A84C] hover:text-[#E8C96A] transition-colors"
          >
            INSTAGRAM
          </a>
        </div>
      </div>
    </nav>
  )
}

/* ─── Hero Section ─── */
function HeroSection() {
  const sectionRef = useRef<HTMLDivElement>(null)
  const titleRef = useRef<HTMLHeadingElement>(null)
  const subRef = useRef<HTMLParagraphElement>(null)
  const lineRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    const tl = gsap.timeline({ delay: 0.5 })
    tl.fromTo(lineRef.current, { width: 0 }, { width: 120, duration: 0.8, ease: 'power2.out' })
    tl.fromTo(titleRef.current, { clipPath: 'inset(0 100% 0 0)', opacity: 0 }, { clipPath: 'inset(0 0% 0 0)', opacity: 1, duration: 1.2, ease: 'power3.inOut' }, '-=0.3')
    tl.fromTo(subRef.current, { opacity: 0, y: 20 }, { opacity: 1, y: 0, duration: 0.8, ease: 'power2.out' }, '-=0.4')
  }, [])

  return (
    <section
      ref={sectionRef}
      className="relative min-h-screen flex flex-col items-center justify-center overflow-hidden"
      style={{ background: 'radial-gradient(ellipse at 50% 50%, rgba(201, 168, 76, 0.06) 0%, transparent 70%)' }}
    >
      <div className="relative z-10 text-center px-6">
        <div ref={lineRef} className="h-[1px] bg-[#C9A84C]/40 mx-auto mb-8" style={{ width: 0 }} />
        <h1
          ref={titleRef}
          className="font-display font-bold text-[#F5F0E8] mb-6"
          style={{
            fontSize: 'clamp(48px, 8vw, 96px)',
            letterSpacing: '-0.02em',
            textShadow: '0 0 80px rgba(201, 168, 76, 0.3)',
          }}
        >
          Happy Birthday Pransh
        </h1>
        <p
          ref={subRef}
          className="text-[#C9A84C] tracking-[0.15em] uppercase font-mono"
          style={{ fontSize: 'clamp(12px, 2vw, 16px)' }}
        >
          A celebration of you
        </p>
      </div>

      <div className="absolute bottom-10 left-1/2 -translate-x-1/2 z-10 flex flex-col items-center gap-2 opacity-50">
        <svg width="20" height="20" viewBox="0 0 20 20" fill="none" className="animate-bounce">
          <path d="M10 4v12m0 0l-4-4m4 4l4-4" stroke="#F5F0E8" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
        </svg>
        <span className="font-mono text-[9px] tracking-widest text-[#F5F0E8]/50">SCROLL TO EXPLORE</span>
      </div>
    </section>
  )
}

/* ─── Profile Section ─── */
function ProfileSection() {
  const sectionRef = useRef<HTMLDivElement>(null)
  const imageRef = useRef<HTMLDivElement>(null)
  const textRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    const triggers: ScrollTrigger[] = []

    triggers.push(
      ScrollTrigger.create({
        trigger: sectionRef.current,
        start: 'top 80%',
        onEnter: () => {
          gsap.fromTo(imageRef.current, { opacity: 0, x: -60, scale: 0.9 }, { opacity: 1, x: 0, scale: 1, duration: 1, ease: 'power2.out' })
          gsap.fromTo(textRef.current, { opacity: 0, x: 60 }, { opacity: 1, x: 0, duration: 1, ease: 'power2.out', delay: 0.2 })
        },
        once: true,
      })
    )

    return () => { triggers.forEach(t => t.kill()) }
  }, [])

  return (
    <section ref={sectionRef} className="relative z-10 min-h-screen flex items-center py-24 px-6">
      <div className="max-w-[1200px] mx-auto w-full grid md:grid-cols-2 gap-16 items-center">
        <div ref={imageRef} className="flex justify-center">
          <div className="relative">
            <div className="w-72 h-72 md:w-96 md:h-96 rounded-full overflow-hidden border-2 border-[#C9A84C]/50 shadow-2xl shadow-[#C9A84C]/10">
              <img
                src="/images/post_2.jpg"
                alt="Pransh"
                className="w-full h-full object-cover"
              />
            </div>
            <div className="absolute -bottom-4 -right-4 w-24 h-24 rounded-full overflow-hidden border-2 border-[#C9A84C] shadow-lg">
              <img
                src="/images/profile_pic.jpg"
                alt="Pransh profile"
                className="w-full h-full object-cover"
              />
            </div>
          </div>
        </div>

        <div ref={textRef} className="text-left">
          <span className="font-mono text-xs tracking-[0.15em] text-[#C9A84C] uppercase mb-4 block">
            The Birthday Boy
          </span>
          <h2 className="font-display font-bold text-[#F5F0E8] mb-4" style={{ fontSize: 'clamp(32px, 4vw, 48px)' }}>
            Bipransh Goswami
          </h2>
          <p className="font-mono text-sm text-[#C9A84C] mb-6">@ibipranshgoswami</p>
          <div className="flex gap-8 mb-8">
            <div>
              <span className="font-display font-bold text-2xl text-[#F5F0E8]">2,805</span>
              <span className="block text-xs text-[#F5F0E8]/50 mt-1 font-mono">FOLLOWERS</span>
            </div>
            <div>
              <span className="font-display font-bold text-2xl text-[#F5F0E8]">289</span>
              <span className="block text-xs text-[#F5F0E8]/50 mt-1 font-mono">POSTS</span>
            </div>
            <div>
              <span className="font-display font-bold text-2xl text-[#F5F0E8]">Today</span>
              <span className="block text-xs text-[#F5F0E8]/50 mt-1 font-mono">BIRTHDAY</span>
            </div>
          </div>
          <p className="text-[#F5F0E8]/70 leading-relaxed max-w-md">
            A creative soul with an eye for stunning photography. From temple visits to mountain adventures,
            every post tells a story of a life lived boldly and beautifully.
          </p>
        </div>
      </div>
    </section>
  )
}

/* ─── Gallery Section ─── */
function GallerySection() {
  const sectionRef = useRef<HTMLDivElement>(null)
  const titleRef = useRef<HTMLHeadingElement>(null)
  const galleryRef = useRef<HTMLDivElement>(null)

  const photos = [
    { src: '/images/post_1.jpg', alt: 'Temple visit' },
    { src: '/images/post_2.jpg', alt: 'Portrait' },
    { src: '/images/post_3.jpg', alt: 'Red shirt' },
    { src: '/images/post_4.jpg', alt: 'Mountain smile' },
    { src: '/images/post_5.jpg', alt: 'Traditional attire' },
    { src: '/images/post_6.jpg', alt: 'Flowers' },
  ]

  useEffect(() => {
    const triggers: ScrollTrigger[] = []

    triggers.push(
      ScrollTrigger.create({
        trigger: sectionRef.current,
        start: 'top 80%',
        onEnter: () => {
          gsap.fromTo(titleRef.current, { opacity: 0, y: 30 }, { opacity: 1, y: 0, duration: 0.8, ease: 'power2.out' })
        },
        once: true,
      })
    )

    // Gallery entrance
    const items = galleryRef.current?.querySelectorAll('.gallery-item')
    if (items) {
      gsap.set(items, { opacity: 0, y: 60, scale: 0.95 })
      triggers.push(
        ScrollTrigger.create({
          trigger: galleryRef.current,
          start: 'top 80%',
          onEnter: () => {
            gsap.to(items, {
              opacity: 1,
              y: 0,
              scale: 1,
              duration: 1,
              stagger: 0.1,
              ease: 'power2.out',
            })
          },
          once: true,
        })
      )
    }

    return () => { triggers.forEach(t => t.kill()) }
  }, [])

  return (
    <section id="gallery" ref={sectionRef} className="relative z-10 py-24 px-6">
      <div className="max-w-[1400px] mx-auto">
        <div className="text-center mb-16">
          <h2
            ref={titleRef}
            className="font-display font-bold text-[#F5F0E8] inline-block"
            style={{ fontSize: 'clamp(36px, 5vw, 56px)' }}
          >
            Moments
          </h2>
          <div className="h-[2px] w-16 bg-[#C9A84C] mx-auto mt-4" />
        </div>

        <div ref={galleryRef} className="grid grid-cols-2 md:grid-cols-3 gap-4 md:gap-6">
          {photos.map((photo, i) => (
            <div
              key={i}
              className="gallery-item group relative overflow-hidden rounded-xl aspect-square cursor-pointer"
            >
              <img
                src={photo.src}
                alt={photo.alt}
                className="w-full h-full object-cover transition-transform duration-700 group-hover:scale-110"
              />
              <div className="absolute inset-0 bg-gradient-to-t from-[#0B0D17]/80 via-transparent to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-500" />
              <div className="absolute bottom-4 left-4 opacity-0 group-hover:opacity-100 transition-all duration-500 translate-y-4 group-hover:translate-y-0">
                <span className="font-mono text-[10px] tracking-wider text-[#C9A84C] uppercase">
                  {photo.alt}
                </span>
              </div>
            </div>
          ))}
        </div>
      </div>
    </section>
  )
}

/* ─── Message Section ─── */
function MessageSection() {
  const sectionRef = useRef<HTMLDivElement>(null)
  const leftRef = useRef<HTMLDivElement>(null)
  const cardRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    const triggers: ScrollTrigger[] = []

    triggers.push(
      ScrollTrigger.create({
        trigger: sectionRef.current,
        start: 'top 75%',
        onEnter: () => {
          gsap.fromTo(leftRef.current, { opacity: 0, x: -40 }, { opacity: 1, x: 0, duration: 0.8, ease: 'power2.out' })
          gsap.fromTo(cardRef.current, { opacity: 0, scale: 0.9, y: 40 }, { opacity: 1, scale: 1, y: 0, duration: 0.8, ease: 'power2.out', delay: 0.2 })
        },
        once: true,
      })
    )

    return () => { triggers.forEach(t => t.kill()) }
  }, [])

  return (
    <section
      id="message"
      ref={sectionRef}
      className="relative z-10 min-h-screen flex items-center py-24 px-6"
      style={{ background: 'linear-gradient(180deg, #0B0D17 0%, #121530 100%)' }}
    >
      <div className="max-w-[1200px] mx-auto w-full grid md:grid-cols-[55%_45%] gap-12 items-center">
        <div ref={leftRef}>
          <h2
            className="font-display font-bold text-[#F5F0E8] mb-4"
            style={{ fontSize: 'clamp(36px, 5vw, 64px)' }}
          >
            Happy Birthday
            <br />
            <span className="text-[#C9A84C]">Pransh</span>
          </h2>
          <p className="font-mono text-sm text-[#C9A84C] tracking-wider mb-8">
            May 15, 2026
          </p>
          <p className="text-[#F5F0E8]/80 leading-[1.7] max-w-md text-lg">
            Today we celebrate you — your creativity, your energy, and the light you bring to everyone
            around you. Here's to another year of amazing adventures, stunning photos, and unforgettable
            memories. Keep shining, keep inspiring.
          </p>
        </div>

        <div ref={cardRef} className="glass-card p-8 md:p-12">
          <div className="flex items-center gap-4 mb-8">
            <div className="w-16 h-16 rounded-full overflow-hidden border-2 border-[#C9A84C]">
              <img src="/images/profile_pic.jpg" alt="Pransh" className="w-full h-full object-cover" />
            </div>
            <div>
              <h3 className="font-display font-bold text-xl text-[#F5F0E8]">Wishing You</h3>
              <p className="font-mono text-xs text-[#C9A84C]">@ibipranshgoswami</p>
            </div>
          </div>

          <ul className="space-y-4 mb-10">
            {[
              'Endless joy and laughter',
              'Success in every endeavor',
              'Adventures worth capturing',
              'Love that lights your way',
            ].map((wish, i) => (
              <li key={i} className="flex items-center gap-3 text-[#F5F0E8]/70">
                <span className="text-[#C9A84C]">&#10022;</span>
                <span>{wish}</span>
              </li>
            ))}
          </ul>

          <a
            href="https://www.instagram.com/ibipranshgoswami"
            target="_blank"
            rel="noopener noreferrer"
            className="btn-gold inline-flex items-center justify-center w-full h-[52px] rounded-full gap-2"
          >
            <span>Follow on Instagram</span>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M7 17L17 7M17 7H7M17 7V17" />
            </svg>
          </a>
        </div>
      </div>
    </section>
  )
}

/* ─── Footer ─── */
function Footer() {
  return (
    <footer className="relative z-10 py-12 px-6 border-t border-[#C9A84C]/10">
      <div className="max-w-[1200px] mx-auto flex flex-col md:flex-row items-center justify-between gap-4">
        <span className="font-mono text-xs tracking-[0.2em] text-[#C9A84C]">
          PRANSH
        </span>
        <p className="text-xs text-[#F5F0E8]/40 font-mono">
          Made with love for Pransh&apos;s birthday &middot; 2026
        </p>
        <a
          href="https://www.instagram.com/ibipranshgoswami"
          target="_blank"
          rel="noopener noreferrer"
          className="text-xs text-[#C9A84C]/60 hover:text-[#C9A84C] transition-colors font-mono"
        >
          @ibipranshgoswami
        </a>
      </div>
    </footer>
  )
}

/* ─── Main App ─── */
export default function App() {
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    // Initialize Lenis smooth scroll
    const lenis = new Lenis({
      lerp: 0.08,
      smoothWheel: true,
    })

    function raf(time: number) {
      lenis.raf(time * 1000)
      requestAnimationFrame(raf)
    }
    requestAnimationFrame(raf)

    // Sync with GSAP ScrollTrigger
    lenis.on('scroll', ScrollTrigger.update)
    gsap.ticker.add((time) => {
      lenis.raf(time * 1000)
    })

    // Simulate loading
    const timer = setTimeout(() => {
      setLoading(false)
    }, 1500)

    return () => {
      clearTimeout(timer)
      lenis.destroy()
    }
  }, [])

  if (loading) {
    return (
      <div className="fixed inset-0 bg-[#0B0D17] flex items-center justify-center z-[100]">
        <p className="font-mono text-xs tracking-[0.15em] text-[#C9A84C] animate-pulse">
          Loading memories...
        </p>
      </div>
    )
  }

  return (
    <div className="relative min-h-screen bg-[#0B0D17]">
      <ParticleBackground />
      <ConfettiCanvas />
      <Navigation />

      <main>
        <HeroSection />
        <ProfileSection />
        <GallerySection />
        <MessageSection />
      </main>

      <Footer />
    </div>
  )
}
