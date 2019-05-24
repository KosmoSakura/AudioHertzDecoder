package cos.mos.kjni.decode;

import com.musicg.api.DetectionApi;
import com.musicg.math.rank.ArrayRankDouble;
import com.musicg.math.statistics.StandardDeviation;
import com.musicg.math.statistics.ZeroCrossingRate;
import com.musicg.wave.Wave;
import com.musicg.wave.WaveHeader;
import com.musicg.wave.extension.Spectrogram;

import cos.mos.kjni.util.ULog;

/**
 * @Description: 口哨音校验
 * @Author: Kosmos
 * @Date: 2019.05.24 10:11
 * @Email: KosmoSakura@gmail.com
 * @apiNote 频率判断
 * 1.降噪
 * 2.滤波
 * 3.交叉
 * 4.声压和频限
 */
public class WhistleCheck extends DetectionApi {
    public WhistleCheck(WaveHeader waveHeader) {
        super(waveHeader);
    }

    /**
     * 吴哥口哨样本：
     * 主频：1933-2791Hz
     * 主频相对音量：-24DB
     * 交叉：39ct
     * 办公室的底噪:1-19
     * 口哨阈值：45-226
     * ----------------
     * 我口哨样本:
     * 主频：2025-2980Hz
     * 主频相对音量：-46DB
     * 交叉：52ct
     * 办公室的底噪:1-19
     * 口哨阈值：58-915
     * 录音机
     * Sound Recorder
     */
    @Override
    protected void init() {
        this.minFrequency = 1800;//低频,参考值:600.0D
        this.maxFrequency = 2650;//高频,参考值:1.7976931348623157E308D
        this.minIntensity = 0.2D;//底声压,参考值:100.0D
        this.maxIntensity = 1.0D;//高声压,参考值: 100000.0D
        this.minStandardDeviation = 0.07D;//最小标准差,参考值:0.10000000149011612D
        this.maxStandardDeviation = 1.0D;//最大标准差,参考值:1.0D
        this.highPass = 100;//低通高切,参考值:100
        this.lowPass = 10000;//高通低切,参考值:10000
        this.minNumZeroCross = 190;//最小数字零交叉,参考值:50
        this.maxNumZeroCross = 300;//最大数字零交叉,参考值:200
        this.numRobust = 10;//主频带宽,参考值:10
    }

    public boolean isWhistle(byte[] audioBytes) {
        return isSpecificSound(audioBytes);
    }

    @Override
    public boolean isSpecificSound(byte[] audioBytes) {
        int bytesPerSample = this.waveHeader.getBitsPerSample() / 8;
        int numSamples = audioBytes.length / bytesPerSample;
        if (numSamples > 0 && Integer.bitCount(numSamples) == 1) {
            this.fftSampleSize = numSamples;
            this.numFrequencyUnit = this.fftSampleSize / 2;
            this.unitFrequency = (double) this.waveHeader.getSampleRate() / 2.0D / (double) this.numFrequencyUnit;
            this.lowerBoundary = (int) ((double) this.highPass / this.unitFrequency);
            this.upperBoundary = (int) ((double) this.lowPass / this.unitFrequency);
            Wave wave = new Wave(this.waveHeader, audioBytes);
            short[] amplitudes = wave.getSampleAmplitudes();
            Spectrogram spectrogram = wave.getSpectrogram(this.fftSampleSize, 0);
            double[][] spectrogramData = spectrogram.getAbsoluteSpectrogramData();
            double[] spectrum = spectrogramData[0];
            int frequencyUnitRange = this.upperBoundary - this.lowerBoundary + 1;
            double[] rangedSpectrum = new double[frequencyUnitRange];
            System.arraycopy(spectrum, this.lowerBoundary, rangedSpectrum, 0, rangedSpectrum.length);
            if (frequencyUnitRange <= spectrum.length) {
                double robustFrequency = getFrequency(rangedSpectrum);
                double numZeroCrosses = getZeroCrossingRate(amplitudes);
                double sd = getStandardDeviation(spectrogramData);
                double intensity = getIntensity(spectrum);
                if (robustFrequency >= minFrequency && robustFrequency <= maxFrequency &&
                    numZeroCrosses >= minNumZeroCross && numZeroCrosses <= maxNumZeroCross &&
                    sd >= minStandardDeviation && sd <= maxStandardDeviation &&
                    intensity > minIntensity && intensity <= maxIntensity) {
                    ULog.commonE("频率=" + robustFrequency + "【" + minFrequency + "-" + maxFrequency + "】" +
                        "|||零交叉=" + numZeroCrosses + "【" + minNumZeroCross + "-" + maxNumZeroCross + "】" +
                        "|||标准差=" + sd + "【" + minStandardDeviation + "-" + maxStandardDeviation + "】" +
                        "|||声压=" + intensity + "【" + minIntensity + "-" + maxIntensity + "】"
                    );
                    return true;
                } else {
                    ULog.commonD("频率=" + robustFrequency + "【" + minFrequency + "-" + maxFrequency + "】" +
                        "|||零交叉=" + numZeroCrosses + "【" + minNumZeroCross + "-" + maxNumZeroCross + "】" +
                        "|||标准差=" + sd + "【" + minStandardDeviation + "-" + maxStandardDeviation + "】" +
                        "|||声压=" + intensity + "【" + minIntensity + "-" + maxIntensity + "】"
                    );
                }


//                if (isPassedIntensity(spectrum) && isPassedStandardDeviation(spectrogramData) &&
//                    isPassedZeroCrossingRate(amplitudes) && isPassedFrequency(rangedSpectrum)) {
//                    ULog.commonE("频率=" + (int) getFrequency(rangedSpectrum) + "【" + minFrequency + "-" + maxFrequency + "】" +
//                            "|||零交叉=" + (int) getZeroCrossingRate(amplitudes) + "【" + minNumZeroCross + "-" + maxNumZeroCross + "】" +
//                            "|||标准差=" + (int) getStandardDeviation(spectrogramData) + "【" + minStandardDeviation + "-" + maxStandardDeviation + "】" +
//                            "|||声压=" + getIntensity(spectrum) + "【" + minIntensity + "-" + maxIntensity + "】"
//                        , "Sakura");
//                    return true;
//                } else {
//                    ULog.commonE("频率=" + (int) getFrequency(rangedSpectrum) + "【" + minFrequency + "-" + maxFrequency + "】" +
//                            "|||零交叉=" + (int) getZeroCrossingRate(amplitudes) + "【" + minNumZeroCross + "-" + maxNumZeroCross + "】" +
//                            "|||标准差=" + getStandardDeviation(spectrogramData) + "【" + minStandardDeviation + "-" + maxStandardDeviation + "】" +
//                            "|||声压=" + getIntensity(spectrum) + "【" + minIntensity + "-" + maxIntensity + "】"
//                        , "Sakura");
//                }
            } else {
                System.err.println("is error: the wave needed to be higher sample rate");
            }
        } else {
            System.out.println("The sample size must be a power of 2");
        }
        return false;
    }

    /**
     * @param spectrum 声谱数组
     * @return 获取标声压
     */
    private double getIntensity(double[] spectrum) {
        double intensity = 0.0D;//声压
        for (int i = 0; i < spectrum.length; ++i) {
            intensity += spectrum[i];
        }
        intensity /= (double) spectrum.length;
        return intensity;
    }

    /**
     * @param spectrum 声谱数组
     * @return 声压是否在预定范围
     */
    @Override
    protected boolean isPassedIntensity(double[] spectrum) {
        double intensity = getIntensity(spectrum);
        return intensity > minIntensity && intensity <= maxIntensity;
    }

    /**
     * @param spectrogramData 声谱图数据
     * @return 获取标准差
     */
    private double getStandardDeviation(double[][] spectrogramData) {
        normalizeSpectrogramData(spectrogramData);
        double[] spectrum = spectrogramData[spectrogramData.length - 1];
        double[] robustFrequencies = new double[numRobust];
        ArrayRankDouble arrayRankDouble = new ArrayRankDouble();
        double nthValue = arrayRankDouble.getNthOrderedValue(spectrum, numRobust, false);
        int count = 0;

        for (int i = 0; i < spectrum.length; ++i) {
            if (spectrum[i] >= nthValue) {
                robustFrequencies[count++] = spectrum[i];
                if (count >= numRobust) {
                    break;
                }
            }
        }

        StandardDeviation standardDeviation = new StandardDeviation();
        standardDeviation.setValues(robustFrequencies);
        return standardDeviation.evaluate();
    }

    /**
     * @param spectrogramData 声谱图数据
     * @return 标准差是否在预定范围
     */
    @Override
    protected boolean isPassedStandardDeviation(double[][] spectrogramData) {
        double sd = getStandardDeviation(spectrogramData);
        return sd >= minStandardDeviation && sd <= maxStandardDeviation;
    }

    /**
     * @param amplitudes 振幅
     * @return 振幅零交叉
     */
    private double getZeroCrossingRate(short[] amplitudes) {
        return new ZeroCrossingRate(amplitudes, 1.0D).evaluate();
    }

    /**
     * @param amplitudes 振幅
     * @return 振幅是否在零交叉范围
     */
    @Override
    protected boolean isPassedZeroCrossingRate(short[] amplitudes) {
        int numZeroCrosses = (int) getZeroCrossingRate(amplitudes);
        return numZeroCrosses >= minNumZeroCross && numZeroCrosses <= maxNumZeroCross;
    }

    /**
     * @param spectrum 声谱数组
     * @return 声谱频率
     */
    private double getFrequency(double[] spectrum) {
        return new ArrayRankDouble().getMaxValueIndex(spectrum) * unitFrequency;
    }

    /**
     * @param spectrum 声谱数组
     * @return 声谱频率是否在预定范围
     */
    @Override
    protected boolean isPassedFrequency(double[] spectrum) {
        double robustFrequency = getFrequency(spectrum);
        return robustFrequency >= minFrequency && robustFrequency <= maxFrequency;
    }
}
