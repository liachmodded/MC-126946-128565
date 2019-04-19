package com.github.liachmodded.datapacks;

public interface FunctionExecutionCallback {
  FunctionExecutionCallback EMPTY = new FunctionExecutionCallback() {
    @Override
    public void finishExecution(int numberExecuted) {
    }

    @Override
    public FunctionExecutionCallback after(FunctionExecutionCallback another) {
      return another;
    }
  };

  void finishExecution(int numberExecuted);

  default FunctionExecutionCallback after(FunctionExecutionCallback another) {
    return (i) -> {
      another.finishExecution(i);
      finishExecution(i);
    };
  }
}
