package cos.mos.kjni.util;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;

/**
 * @Description: 实验室
 * @Author: Kosmos
 * @Date: 2019.05.14 16:33
 * @Email: KosmoSakura@gmail.com
 */
public class Laboratory {
    private static Disposable subscribe;
    private static boolean sss;

    public static void main(String[] args) {
        log("开始执行RxJava");
        sss = true;
        Disposable subscribe = Observable.interval(1, TimeUnit.SECONDS, Schedulers.trampoline())
//            .take(10)
            .takeWhile(new Predicate<Long>() {
                @Override
                public boolean test(Long aLong) throws Exception {
                    return sss;
                }
            })
            .subscribe(new Consumer<Long>() {
                @Override
                public void accept(Long aLong) throws Exception {
                    log("->" + aLong);
                    if (aLong == 5) {
                        sss = false;
                    }
                }
            });
    }

    private static void log(String str) {
        System.out.println(str + ",线程名：" + Thread.currentThread().getName());
    }
}
