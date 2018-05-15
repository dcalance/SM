using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Media;
using System.IO;
using NAudio.Wave;
using NAudio.Wave.SampleProviders;

namespace Lab3
{
    class Song
    {
        public Song(String input)
        {
            data = File.ReadAllBytes(input);
        }

        byte[] data;
        public bool isInverted = false;
        public float speed = 1f;
        public string pitch = "0";
        List<WaveOut> waveOut = new List<WaveOut>();
        public void play()
        {
            byte[] processedSong = new byte[data.Length];
            Array.Copy(data, processedSong, data.Length);
            if (isInverted)
            {
                processedSong = invert(processedSong);
            }

            Stream stream = new MemoryStream(processedSong);
            var reader = new WaveFileReader(new MemoryStream(processedSong));
            var newWaveOut = new WaveOut();

            switch (pitch)
            {
                case "-1":
                    using (var reader1 = new StreamMediaFoundationReader(stream))
                    {
                        var pitch1 = new SmbPitchShiftingSampleProvider(reader1.ToSampleProvider());
                        pitch1.PitchFactor = 0.5f;
                        newWaveOut.Init(pitch1);
                    }
                    break;
                case "1":
                    using (var reader1 = new StreamMediaFoundationReader(stream))
                    {
                        var pitch1 = new SmbPitchShiftingSampleProvider(reader1.ToSampleProvider());
                        pitch1.PitchFactor = 2.0f;
                        newWaveOut.Init(pitch1);
                    }
                    break;
                default:
                    newWaveOut.Init(reader);
                    break;
            }
            newWaveOut.Play();
            waveOut.Add(newWaveOut);
        }
        public void stop()
        {
            if (waveOut.Count > 0)
            {
                waveOut[waveOut.Count - 1].Stop();
                waveOut.RemoveAt(waveOut.Count - 1);
            }
        }

        byte[] invert(byte[] array)
        {
            var reverseWav = new ReverseWav();
            return reverseWav.reverse(array);
        }

    }
}

