<template>
  <div class="home-container">
    <!-- Three.js Canvas Container -->
    <div ref="canvasContainer" class="canvas-container"></div>
    
    <!-- Main Content -->
    <div class="content-wrapper">
      <div class="hero-section">
        <div class="hero-content">
          <h1>DB Master 数据管理系统</h1>
          <p class="subtitle">高效、安全的数据库管理与协作解决方案</p>
          <div class="cta-buttons">
            <a-button type="primary" size="large" href="/login">
              登录系统
            </a-button>
            <a-button size="large" href="/register" style="margin-left: 16px;">
              免费注册
            </a-button>
          </div>
        </div>
      </div>
      
      <!-- Project Introduction Section -->
      <div class="introduction-section">
        <h2>项目介绍</h2>
        <div class="introduction-content">
          <p>基于 Spring AI 集成框架构建的智能数据库管理系统，采用智谱基础大模型解析自然语言，结合 Tool Calling、Redis 轻量矢量数据库、RAG 及上下文技术实现聊天服务，支持通过对话完成项目创建、数据库结构记录、数据结构匹配、查询及语句生成，支持操作 Gbase、PostgreSQL 等国产数据库。</p>
          <div class="introduction-highlights">
            <div class="highlight-item">
              <div class="highlight-icon">📊</div>
              <div class="highlight-text">多数据库类型支持</div>
            </div>
            <div class="highlight-item">
              <div class="highlight-icon">🤖</div>
              <div class="highlight-text">AI智能助手</div>
            </div>
            <div class="highlight-item">
              <div class="highlight-icon">👥</div>
              <div class="highlight-text">团队协作管理</div>
            </div>
            <div class="highlight-item">
              <div class="highlight-icon">📊</div>
              <div class="highlight-text">操作记录可视化</div>
            </div>
          </div>
        </div>
      </div>
      
      <div class="features-section">
        <h2>平台核心功能</h2>
        <div class="features-grid">
          <a-card hoverable class="feature-card">
            <template #cover>
              <div class="feature-icon-text">🗃️</div>
            </template>
            <a-card-meta title="数据库管理" description="轻松创建、管理和维护各类数据库项目" />
          </a-card>
          
          <a-card hoverable class="feature-card">
            <template #cover>
              <div class="feature-icon-text">🤖</div>
            </template>
            <a-card-meta title="智能助手" description="AI驱动的数据库问题解答和优化建议" />
          </a-card>
          
          <a-card hoverable class="feature-card">
            <template #cover>
              <div class="feature-icon-text">📈</div>
            </template>
            <a-card-meta title="版本迭代可视化" description="直观展示数据库结构和内容的变更历史" />
          </a-card>
          
          <a-card hoverable class="feature-card">
            <template #cover>
              <div class="feature-icon-text">🏗️</div>
            </template>
            <a-card-meta title="数据结构管理" description="可视化编辑和优化数据库表结构" />
          </a-card>
        </div>
      </div>
      
      <!-- Tech Stack Section -->
      <div class="tech-stack-section">
        <h2>核心技术栈</h2>
        <div class="tech-stack-scroll">
          <div class="tech-item">SpringBoot</div>
          <div class="tech-item">Vue</div>
          <div class="tech-item">SaToken</div>
          <div class="tech-item">MySQL</div>
          <div class="tech-item">Nginx</div>
          <div class="tech-item">Redis Search</div>
          <div class="tech-item">MinIO</div>
          <div class="tech-item">Nacos</div>
          <div class="tech-item">Docker</div>
        </div>
      </div>
      
      <!-- Developer Information Section -->
      <div class="developer-section">
        <div class="developer-content">
          <p>开发人员：SHUZHI</p>
          <p>联系邮箱：15372542572@163.com</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, onUnmounted } from 'vue';
import { Button, Card } from 'ant-design-vue';
import * as THREE from 'three';
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls';

export default {
  name: 'HomeView',
  components: {
    'a-button': Button,
    'a-card': Card,
    'a-card-meta': Card.Meta
  },
  setup() {
    const canvasContainer = ref(null);
    let scene, camera, renderer, controls, particles, animationFrameId;
    let particleCount = 2000;
    
    // Initialize Three.js scene
    const initThreeJs = () => {
      scene = new THREE.Scene();
      scene.background = new THREE.Color(0xffffff);
      
      // Create camera
      camera = new THREE.PerspectiveCamera(75, window.innerWidth / window.innerHeight, 0.1, 1000);
      camera.position.z = 50;
      camera.position.y = 20;
      
      // Create renderer
      renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true });
      renderer.setSize(window.innerWidth, window.innerHeight);
      renderer.setPixelRatio(window.devicePixelRatio);
      
      // Add to DOM
      if (canvasContainer.value) {
        canvasContainer.value.appendChild(renderer.domElement);
      }
      
      // Add controls
      controls = new OrbitControls(camera, renderer.domElement);
      controls.enableDamping = true;
      controls.dampingFactor = 0.05;
      controls.enableZoom = false;
      controls.autoRotate = true;
      controls.autoRotateSpeed = 0.5;
      
      // Create grid
      const gridHelper = new THREE.GridHelper(100, 30, 0xd1e8ff, 0xd1e8ff);
      scene.add(gridHelper);
      
      // Create axes helper
      const axesHelper = new THREE.AxesHelper(50);
      scene.add(axesHelper);
      
      // Create particle field
      createParticleField();
      
      // Create database-like structures
      createDatabaseStructures();
      
      // Handle window resize
      window.addEventListener('resize', handleResize);
      
      // Start animation
      animate();
    };
    
    // Create particle field
    const createParticleField = () => {
      const particlesGeometry = new THREE.BufferGeometry();
      const posArray = new Float32Array(particleCount * 3);
      const colorArray = new Float32Array(particleCount * 3);
      
      for (let i = 0; i < particleCount * 3; i++) {
        posArray[i] = (Math.random() - 0.5) * 200;
        
        // Create a gradient of white to light blue for particles
        if (i % 3 === 0) {
          // Red component
          colorArray[i] = 1.0;
        } else if (i % 3 === 1) {
          // Green component
          colorArray[i] = 1.0 - Math.random() * 0.1;
        } else {
          // Blue component
          colorArray[i] = 0.95 - Math.random() * 0.2;
        }
      }
      
      particlesGeometry.setAttribute('position', new THREE.BufferAttribute(posArray, 3));
      particlesGeometry.setAttribute('color', new THREE.BufferAttribute(colorArray, 3));
      
      const particlesMaterial = new THREE.PointsMaterial({
        size: 0.5,
        vertexColors: true,
        transparent: true,
        opacity: 0.8,
        sizeAttenuation: true
      });
      
      particles = new THREE.Points(particlesGeometry, particlesMaterial);
      scene.add(particles);
    };
    
    // Create database-like structures
    const createDatabaseStructures = () => {
      // Create main database structure with gradient effect
      const mainGeo = new THREE.BoxGeometry(20, 15, 20);
      const mainMat = new THREE.MeshBasicMaterial({
        color: 0x5ac8fa,
        transparent: true,
        opacity: 0.25,
        wireframe: true
      });
      const mainBox = new THREE.Mesh(mainGeo, mainMat);
      scene.add(mainBox);
      
      // Add glow effect to main structure
      const glowGeo = new THREE.BoxGeometry(22, 17, 22);
      const glowMat = new THREE.MeshBasicMaterial({
        color: 0x81d4fa,
        transparent: true,
        opacity: 0.15,
        wireframe: true
      });
      const glowBox = new THREE.Mesh(glowGeo, glowMat);
      scene.add(glowBox);
      
      // Create connected structures with light blue colors
      const colors = [0x5ac8fa, 0x81d4fa, 0x90caf9, 0xa5d8ff, 0xc5e1ff, 0xd1e8ff];
      
      for (let i = 0; i < 6; i++) {
        const angle = (i / 6) * Math.PI * 2;
        const distance = 35;
        
        const geo = new THREE.BoxGeometry(10, 8, 10);
        const mat = new THREE.MeshBasicMaterial({
          color: colors[i],
          transparent: true,
          opacity: 0.2,
          wireframe: true
        });
        const box = new THREE.Mesh(geo, mat);
        
        box.position.x = Math.cos(angle) * distance;
        box.position.z = Math.sin(angle) * distance;
        
        scene.add(box);
        
        // Create animated connection lines
        const lineGeo = new THREE.BufferGeometry().setFromPoints([
          new THREE.Vector3(0, 0, 0),
          new THREE.Vector3(box.position.x, box.position.y, box.position.z)
        ]);
        const lineMat = new THREE.LineBasicMaterial({ color: colors[i], opacity: 0.5, transparent: true });
        const line = new THREE.Line(lineGeo, lineMat);
        scene.add(line);
      }
    };
    
    // Handle window resize
    const handleResize = () => {
      camera.aspect = window.innerWidth / window.innerHeight;
      camera.updateProjectionMatrix();
      renderer.setSize(window.innerWidth, window.innerHeight);
    };
    
    // Animation loop
    const animate = () => {
      animationFrameId = requestAnimationFrame(animate);
      
      // Update controls
      if (controls) controls.update();
      
      // Animate particles with wave effect
      if (particles) {
        particles.rotation.x += 0.0005;
        particles.rotation.y += 0.0005;
        
        // Create a pulsing effect for particles
        const time = Date.now() * 0.001;
        const positions = particles.geometry.attributes.position.array;
        const colors = particles.geometry.attributes.color.array;
        
        for (let i = 0; i < positions.length; i += 3) {
          // Make particles float up and down slightly
          positions[i + 1] += Math.sin(time + i * 0.1) * 0.01;
          
          // Make colors pulse
        if (i % 3 === 2) { // Blue component
          colors[i] = 0.95 + Math.sin(time * 2 + i * 0.1) * 0.05;
        }
        }
        
        particles.geometry.attributes.position.needsUpdate = true;
        particles.geometry.attributes.color.needsUpdate = true;
      }
      
      // Render scene
      renderer.render(scene, camera);
    };
    
    // Clean up
    const cleanup = () => {
      if (animationFrameId) {
        cancelAnimationFrame(animationFrameId);
      }
      
      window.removeEventListener('resize', handleResize);
      
      if (controls) {
        controls.dispose();
      }
      
      if (renderer) {
        renderer.dispose();
        if (canvasContainer.value && renderer.domElement) {
          canvasContainer.value.removeChild(renderer.domElement);
        }
      }
    };
    
    onMounted(() => {
      initThreeJs();
    });
    
    onUnmounted(() => {
      cleanup();
    });
    
    return {
      canvasContainer
    };
  }
}
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  position: relative;
  overflow: hidden;
}

/* Three.js Canvas Container */
.canvas-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: -1;
}

/* Content Wrapper */
.content-wrapper {
  position: relative;
  z-index: 1;
}

/* Hero Section with frosted glass effect */
.hero-section {
  background: rgba(173, 216, 230, 0.1);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  padding: 100px 20px;
  text-align: center;
  border-bottom-left-radius: 30px;
  border-bottom-right-radius: 30px;
}

.hero-content h1 {
  font-size: 52px;
  margin-bottom: 20px;
  font-weight: 700;
  text-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
  background: linear-gradient(to right, #1a73e8 0%, #4285f4 100%);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  display: inline-block;
}

.subtitle {
  font-size: 22px;
  margin-bottom: 40px;
  color: #2c3e50;
  opacity: 0.9;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  max-width: 800px;
  margin-left: auto;
  margin-right: auto;
}

/* Call-to-Action Buttons */
.cta-buttons {
  margin-top: 32px;
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 16px;
}

.cta-buttons a-button {
  border-radius: 50px;
  padding: 12px 32px;
  font-size: 18px;
  font-weight: 500;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  border: none;
}

.cta-buttons a-button:first-child {
  background: linear-gradient(135deg, #1a73e8 0%, #4285f4 100%);
  color: white;
}

.cta-buttons a-button:first-child:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 24px rgba(26, 115, 232, 0.3);
}

.cta-buttons a-button:last-child {
  background: transparent;
  border: 2px solid #1a73e8;
  color: #1a73e8;
}

.cta-buttons a-button:last-child:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 24px rgba(0, 0, 0, 0.2);
  background: rgba(255, 255, 255, 0.1);
}

/* Project Introduction Section with frosted glass effect */
.introduction-section {
  padding: 80px 20px;
  text-align: center;
  background: rgba(173, 216, 230, 0.1);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  margin-top: 60px;
  border-radius: 20px;
  margin-left: 20px;
  margin-right: 20px;
}

.introduction-section h2 {
  font-size: 36px;
  margin-bottom: 30px;
  color: #1a73e8;
  font-weight: 700;
  position: relative;
  display: inline-block;
}

.introduction-section h2::after {
  content: '';
  position: absolute;
  bottom: -15px;
  left: 50%;
  transform: translateX(-50%);
  width: 80px;
  height: 4px;
  background: linear-gradient(90deg, #1a73e8 0%, #4285f4 100%);
  border-radius: 2px;
}

.introduction-content {
  max-width: 1000px;
  margin: 0 auto;
}

.introduction-content p {
  font-size: 18px;
  line-height: 1.8;
  margin-bottom: 24px;
  color: #2c3e50;
  opacity: 0.95;
}

.introduction-highlights {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 30px;
  margin-top: 40px;
}

.highlight-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 120px;
}

.highlight-icon {
  font-size: 42px;
  margin-bottom: 16px;
  opacity: 0.9;
  transition: transform 0.3s ease;
}

.highlight-item:hover .highlight-icon {
  transform: scale(1.2);
}

.highlight-text {
  font-size: 16px;
  font-weight: 500;
  color: #1a73e8;
}

/* Features Section with frosted glass effect */
.features-section {
  padding: 80px 20px;
  text-align: center;
  background: rgba(173, 216, 230, 0.1);
  margin-top: 60px;
  border-radius: 20px;
  margin-left: 20px;
  margin-right: 20px;
}

.features-section h2 {
  font-size: 36px;
  margin-bottom: 48px;
  color: #1a73e8;
  font-weight: 700;
  position: relative;
  display: inline-block;
}

.features-section h2::after {
  content: '';
  position: absolute;
  bottom: -15px;
  left: 50%;
  transform: translateX(-50%);
  width: 80px;
  height: 4px;
  background: linear-gradient(90deg, #1a73e8 0%, #4285f4 100%);
  border-radius: 2px;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 30px;
  max-width: 1500px;
  margin: 0 auto;
}

/* Feature Cards */
.feature-card {
  height: 100%;
  transition: all 0.4s ease;
  background: #ffffff;
  backdrop-filter: blur(10px);
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid rgba(26, 115, 232, 0.1);
  position: relative;
  z-index: 1;
}

.feature-icon-text {
  height: 140px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 64px;
  padding: 24px;
  transition: all 0.3s ease;
}

.feature-card:hover .feature-icon-text {
  transform: scale(1.1);
}

.feature-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 5px;
  background: linear-gradient(90deg, #1a73e8 0%, #4285f4 100%);
  z-index: -1;
}

.feature-card:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow: 0 15px 35px rgba(22, 119, 255, 0.15);
  background: rgba(255, 255, 255, 1);
}

.feature-card:hover .feature-icon {
  transform: scale(1.1);
  filter: brightness(1.2);
}

.feature-icon {
  height: 140px;
  object-fit: contain;
  padding: 24px;
  transition: all 0.3s ease;
}

/* Tech Stack Section */
.tech-stack-section {
  padding: 80px 20px;
  text-align: center;
  background: #f0f9ff;
  margin-top: 60px;
  border-radius: 20px;
  margin-left: 20px;
  margin-right: 20px;
}

.tech-stack-section h2 {
  font-size: 36px;
  margin-bottom: 40px;
  color: #1a73e8;
  font-weight: 700;
  position: relative;
  display: inline-block;
}

.tech-stack-section h2::after {
  content: '';
  position: absolute;
  bottom: -15px;
  left: 50%;
  transform: translateX(-50%);
  width: 80px;
  height: 4px;
  background: linear-gradient(90deg, #1a73e8 0%, #4285f4 100%);
  border-radius: 2px;
}

.tech-stack-scroll {
  display: flex;
  overflow-x: auto;
  gap: 16px;
  padding: 20px 0;
  justify-content: center;
  flex-wrap: wrap;
}

.tech-stack-scroll::-webkit-scrollbar {
  height: 6px;
}

.tech-stack-scroll::-webkit-scrollbar-track {
  background: rgba(173, 216, 230, 0.3);
  border-radius: 3px;
}

.tech-stack-scroll::-webkit-scrollbar-thumb {
  background: rgba(26, 115, 232, 0.5);
  border-radius: 3px;
}

.tech-stack-scroll::-webkit-scrollbar-thumb:hover {
  background: rgba(26, 115, 232, 0.7);
}

.tech-item {
  background: #ffffff;
  border: 1px solid rgba(26, 115, 232, 0.2);
  border-radius: 50px;
  padding: 12px 32px;
  font-size: 18px;
  font-weight: 500;
  color: #1a73e8;
  white-space: nowrap;
  transition: all 0.3s ease;
  backdrop-filter: blur(5px);
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.tech-item:hover {
  background: rgba(26, 115, 232, 0.9);
  color: white;
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(26, 115, 232, 0.3);
}

@media (max-width: 768px) {
  .hero-content h1 {
    font-size: 32px;
  }
  
  .subtitle {
    font-size: 16px;
  }
  
  .cta-buttons {
    flex-direction: column;
    align-items: center;
  }
  
  .cta-buttons a-button {
    margin: 8px 0;
    width: 100%;
    max-width: 240px;
  }
}

/* Developer Section */
.developer-section {
  padding: 40px 20px;
  text-align: center;
  background: #f8fafc;
  border-top: 1px solid #e2e8f0;
  margin-top: 60px;
}

.developer-content {
  max-width: 600px;
  margin: 0 auto;
}

.developer-content p {
  margin: 8px 0;
  font-size: 16px;
  color: #475569;
}
</style>