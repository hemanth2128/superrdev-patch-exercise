import { useState, useEffect } from 'react';
import { fetchTasks } from '../api';

export function useTasks(query, status, page, pageSize) {
  const [tasks, setTasks] = useState([]);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    const controller = new AbortController();

    // Debounce rapid keystrokes so one request fires per pause in typing.
    const timer = setTimeout(() => {
      setLoading(true);
      setError(null);

      fetchTasks({ query, status, page, pageSize }, controller.signal)
        .then((data) => {
          if (controller.signal.aborted) return;
          setTasks(data.items);
          setTotal(data.total);
        })
        .catch((err) => {
          if (controller.signal.aborted || err?.name === 'AbortError') return;
          setError(err.message);
        })
        .finally(() => {
          if (!controller.signal.aborted) setLoading(false);
        });
    }, 300);

    // Cancel pending debounce + in-flight request on filter change/unmount.
    // Prevents stale (slow) responses overwriting fresh results.
    return () => {
      clearTimeout(timer);
      controller.abort();
    };
  }, [query, status, page, pageSize]);

  return { tasks, total, loading, error };
}
