package pamalyshev.photoeffects;

/**
 * Created by pamalyshev on 03.12.15.
 */
public class AsyncResult<T> {
    private T value;
    private Exception exception;

    public AsyncResult(T value) {
        this.value = value;
    }

    public AsyncResult(Exception exception) {
        this.exception = exception;
    }

    public T getValue() {
        return value;
    }

    public Exception getException() {
        return exception;
    }

    public T getValueOrThrow() throws Exception {
        if (exception != null)
            throw exception;
        return value;
    }
}
