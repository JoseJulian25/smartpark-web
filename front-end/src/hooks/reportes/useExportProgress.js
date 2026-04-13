import { useCallback, useState } from "react";

export const useExportProgress = () => {
  const [exporting, setExporting] = useState(false);
  const [progress, setProgress] = useState(0);

  const runWithProgress = useCallback(async (job) => {
    setExporting(true);
    setProgress(12);

    const intervalId = window.setInterval(() => {
      setProgress((current) => {
        if (current >= 88) return current;
        return Math.min(88, current + 8);
      });
    }, 180);

    try {
      const result = await job();
      setProgress(100);
      return result;
    } finally {
      window.clearInterval(intervalId);
      window.setTimeout(() => {
        setExporting(false);
        setProgress(0);
      }, 450);
    }
  }, []);

  return {
    exporting,
    progress,
    runWithProgress,
  };
};
