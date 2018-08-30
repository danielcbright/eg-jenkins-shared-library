def call() {
  def rubyContent = libraryResource('script_test.txt')
  writeFile(file: 'script_text.txt', text: rubyContent)
  sh('chmod +x script_text.txt')
}