<script setup>
defineProps({
  loading: {
    type: Array,
    default: [],
  },
})
</script>

<template>
  <section class="loader w-full h-full object-cover">
    <div class="slider-wrapper">
      <div class="slider" style="--i: 0"></div>
      <div class="slider" style="--i: 1"></div>
      <div class="slider" style="--i: 2"></div>
      <div class="slider" style="--i: 3"></div>
      <div class="slider" style="--i: 4"></div>
    </div>
    <div>{{ loading.join(' , ') }}</div>
  </section>
</template>

<style scoped>
.loader {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column; /* เปลี่ยนเป็น column */
}

.slider {
  overflow: hidden;
  background-color: white;
  margin: 0 15px;
  height: 80px;
  width: 20px;
  border-radius: 30px;
  box-shadow:
    15px 15px 20px rgba(0, 0, 0, 0.1),
    -15px -15px 30px #fff,
    inset -5px -5px 10px rgba(0, 0, 255, 0.1),
    inset 5px 5px 10px rgba(0, 0, 0, 0.1);
  position: relative;
}

.slider-wrapper {
  display: flex;
  flex-direction: row;
  justify-content: center; 
}

.loader > div {
  margin-top: 20px; 
  font-size: 1.2em; 
  text-align: center; 
}

.slider::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  height: 20px;
  width: 20px;
  border-radius: 100%;
  box-shadow:
    inset 0px 0px 0px rgba(0, 0, 0, 0.3),
    0px 420px 0 400px #2697f3,
    inset 0px 0px 0px rgba(0, 0, 0, 0.1);
  animation: animate_2 2.5s ease-in-out infinite;
  animation-delay: calc(-0.5s * var(--i));
}

@keyframes animate_2 {
  0% {
    transform: translateY(250px);
    filter: hue-rotate(0deg);
  }

  50% {
    transform: translateY(0);
  }

  100% {
    transform: translateY(250px);
    filter: hue-rotate(180deg);
  }
}
</style>
