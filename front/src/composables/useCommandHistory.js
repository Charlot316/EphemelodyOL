import { ref } from 'vue';

export function useCommandHistory() {
  const undoStack = ref([]);
  const redoStack = ref([]);
  const maxHistory = 100;

  const pushCommand = (cmd) => {
    // cmd structure: 
    // { 
    //   undo: () => void, 
    //   redo: () => void, 
    //   description?: string 
    // }
    
    //console.log('Pushing command:', cmd.description);
    undoStack.value.push(cmd);
    if (undoStack.value.length > maxHistory) {
      undoStack.value.shift();
    }
    redoStack.value = [];
  };

  const undo = () => {
    if (undoStack.value.length === 0) return;
    const cmd = undoStack.value.pop();
    //console.log('Undoing:', cmd.description);
    cmd.undo();
    redoStack.value.push(cmd);
  };

  const redo = () => {
    if (redoStack.value.length === 0) return;
    const cmd = redoStack.value.pop();
    //console.log('Redoing:', cmd.description);
    cmd.redo();
    undoStack.value.push(cmd);
  };
  
  const clear = () => {
    undoStack.value = [];
    redoStack.value = [];
  };

  return {
    undoStack,
    redoStack,
    pushCommand,
    undo,
    redo,
    clear
  };
}
