<template>
  <div class="backgroung-operation"></div>
</template>

<script>
export default {
  props: ["operation"],
  data(){
    return {
      myOperation: this.operation,
    }
  }
};
</script>

<style scope>
.backgroung-operation {
  height: 50px;
}
</style>
