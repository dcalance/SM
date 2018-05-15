using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;
using Microsoft.Win32;
using System.IO;
using System.Media;

namespace Lab3
{
    public partial class MainWindow : Window
    {
        List<Song> songList = new List<Song>();

        public MainWindow()
        {
            InitializeComponent();
        }

        private void addSongBtn_Click(object sender, RoutedEventArgs e)
        {
            OpenFileDialog openFileDialog = new OpenFileDialog();
            openFileDialog.Filter = "wav files (*.wav)|*.wav|All files (*.*)|*.*";
            if (openFileDialog.ShowDialog() == true)
            {
                listBox.Items.Add(openFileDialog.FileName);
                songList.Add(new Song(openFileDialog.FileName));
                listBox.SelectedIndex = songList.Count - 1;
            }
        }

        private void playBtn_Click(object sender, RoutedEventArgs e)
        {
            if(songList.Count > 0)
            {
                songList[listBox.SelectedIndex].pitch = pitchBox.Text;
                songList[listBox.SelectedIndex].play();
            }
            
        }

        private void isInverted_Click(object sender, RoutedEventArgs e)
        {
            if (isInverted.IsChecked.Value && songList.Count > 0)
            {
                songList[listBox.SelectedIndex].isInverted = true;
            } else if (!isInverted.IsChecked.Value && songList.Count > 0)
            {
                songList[listBox.SelectedIndex].isInverted = false;
            }
        }

        private void stopBtn_Click(object sender, RoutedEventArgs e)
        {
            if (songList.Count > 0)
            {
                songList[listBox.SelectedIndex].stop();
            }
        }

        private void pitchBox_TextChanged(object sender, TextChangedEventArgs e)
        {
           
        }
    }
}
