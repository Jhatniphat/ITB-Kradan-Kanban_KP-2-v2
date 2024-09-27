<script setup>

</script>

<template>
  <div class="h-full w-full">
    <div class="loader mx-auto">
    </div>
  </div>
</template>

<style scoped>
/* HTML: <div class="loader"></div> */
.loader {
  width: 22px;
  aspect-ratio: 1;
  border-radius: 50%;
  background: #00FFBA;
  animation: l10 1.5s infinite linear;
}
@keyframes l10 {
  0%    {box-shadow:
      0 -30px #00C6FF, calc(30px*0.707) calc(-30px*0.707) #FF94FB,30px 0 #00C6FF, 0 0 #FF94FB,
      0 0 #00C6FF, 0 0 #FF94FB,0 0 #00C6FF, 0 0 #FF94FB}
  12.5% {box-shadow:
      0 0 #00C6FF, calc(30px*0.707) calc(-30px*0.707) #FF94FB,30px 0 #00C6FF, calc(30px*0.707) calc(30px*0.707) #FF94FB,
      0 0 #00C6FF, 0 0 #FF94FB,0 0 #00C6FF, 0 0 #FF94FB}
  25%   {box-shadow:
      0 0 #00C6FF, 0 0 #FF94FB,30px 0 #00C6FF, calc(30px*0.707) calc(30px*0.707) #FF94FB,
      0 30px #00C6FF, 0 0 #FF94FB,0 0 #00C6FF, 0 0 #FF94FB}
  37.5% {box-shadow:
      0 0 #00C6FF, 0 0 #FF94FB,0 0 #00C6FF, calc(30px*0.707) calc(30px*0.707) #FF94FB,
      0 30px #00C6FF, calc(-30px*0.707) calc(30px*0.707) #FF94FB,0 0 #00C6FF, 0 0 #FF94FB}
  50%   {box-shadow:
      0 0 #00C6FF, 0 0 #FF94FB,0 0 #00C6FF, 0 0 #FF94FB,
      0 30px #00C6FF, calc(-30px*0.707) calc(30px*0.707) #FF94FB,-30px 0 #00C6FF, 0 0 #FF94FB}
  62.5% {box-shadow:
      0 0 #00C6FF, 0 0 #FF94FB,0 0 #00C6FF, 0 0 #FF94FB,
      0 0 #00C6FF, calc(-30px*0.707) calc(30px*0.707) #FF94FB,-30px 0 #00C6FF, calc(-30px*0.707) calc(-30px*0.707) #FF94FB}
  75%   {box-shadow:
      0 -30px #00C6FF, 0 0 #FF94FB,0 0 #00C6FF, 0 0 #FF94FB,
      0 0 #00C6FF, 0 0 #FF94FB,-30px 0 #00C6FF, calc(-30px*0.707) calc(-30px*0.707) #FF94FB}
  87.5% {box-shadow:
      0 -30px #00C6FF, calc(30px*0.707) calc(-30px*0.707) #FF94FB,0 0 #00C6FF, 0 0 #FF94FB,
      0 0 #00C6FF, 0 0 #FF94FB,0 0 #00C6FF, calc(-30px*0.707) calc(-30px*0.707) #FF94FB}
  100%  {box-shadow:
      0 -30px #00C6FF, calc(30px*0.707) calc(-30px*0.707) #FF94FB,30px 0 #00C6FF, 0 0 #FF94FB,
      0 0 #00C6FF, 0 0 #FF94FB,0 0 #00C6FF, 0 0 #FF94FB}

}
</style>