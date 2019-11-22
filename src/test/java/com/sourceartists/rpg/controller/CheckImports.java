package com.sourceartists.rpg.controller;

import com.sourceartists.rpg.engine.GameEngine;
import com.sourceartists.rpg.exception.HeroSlainedByDragonException;
import com.sourceartists.rpg.model.Dragon;
import com.sourceartists.rpg.model.Hero;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class CheckImports {

    public interface SomeInterface {

        int doSomething(int a);
    }
    public class SomeService {

        private final SomeInterface someInterface;

        public SomeService(SomeInterface someInterface) {
            this.someInterface = someInterface;
        }

        public int callInterface(int... params) {
            int sum = 0;
            for (int param : params) {
                sum += someInterface.doSomething(param); // call for each param
            }
            return sum;
        }
    }

    @Test
    public void myTest() {
        SomeInterface myInterfaceMock = mock(SomeInterface.class);
        SomeService myService = new SomeService(myInterfaceMock);

        doReturn(1).when(myInterfaceMock).doSomething(1);
        doReturn(2).when(myInterfaceMock).doSomething(2);
//        doReturn(3).when(myInterfaceMock).doSomething(3);

        int sum = myService.callInterface(1, 2, 3);

//        assertEquals(1 + 2 + 3, sum);

        verify(myInterfaceMock).doSomething(1);
        verify(myInterfaceMock).doSomething(2);
//        verify(myInterfaceMock).doSomething(3);
        verifyNoMoreInteractions(myInterfaceMock); // succeeds???

    }
}