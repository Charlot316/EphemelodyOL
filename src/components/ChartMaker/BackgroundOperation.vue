<template>
  <div class="backgroung-operation current-operation">

  </div>
</template>

<script>
export default {
  props: ["operation"],
  data() {
    return {
      myOperation: this.operation,
    };
  },
};
</script>

<style scope>
.backgroung-operation {
  height: 80px;
  width: calc(100% - 20px);
  margin:10px;
}

.current-operation {
  border: none;
  -webkit-border-radius: 5px;
  border-radius: 5px;
  background: white;
  -webkit-box-shadow: 0 0 5px 2px rgba(0, 0, 0, 0.5);
  box-shadow: 0 0 5px 2px rgba(0, 0, 0, 0.5);
}
.passed-operation {
  border: none;
  -webkit-border-radius: 5px;
  border-radius: 5px;
  background: #bebebe;
  -webkit-box-shadow: 0 0 9px 4px rgba(0, 0, 0, 0.5);
  box-shadow: 0 0 9px 4px rgba(0, 0, 0, 0.5);
}
.to-come-operation {
  border: none;
  -webkit-border-radius: 5px;
  border-radius: 5px;
  background: white;
  -webkit-box-shadow: 0 0 2px 0 rgba(0, 0, 0, 0.5);
  box-shadow: 0 0 2px 0 rgba(0, 0, 0, 0.5);
}
</style>
