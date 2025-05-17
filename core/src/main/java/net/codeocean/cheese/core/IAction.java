package net.codeocean.cheese.core;

public interface IAction {
    void invoke();
    Object invoke(Object args);
}
