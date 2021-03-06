(ns clojure.io
  (:import (java.io Reader InputStream InputStreamReader
                    BufferedReader File PrintWriter OutputStream
                    OutputStreamWriter BufferedWriter Writer
                    FileInputStream FileOutputStream
                    StringReader ByteArrayInputStream)
           (java.net URI URL MalformedURLException Socket)))

(def
 #^{:doc "Name of the default encoding to use when reading & writing.
  Default is UTF-8."
    :tag String}
 *default-encoding* "UTF-8")

(def
 #^{:doc "Size, in bytes or characters, of the buffer used when
  copying streams."}
 *buffer-size* 1024)

(def
 #^{:doc "Type object for a Java primitive byte array."}
 byte-array-type (class (make-array Byte/TYPE 0)))

(defmulti #^{:tag BufferedReader
             :doc "Attempts to coerce its argument into an open
  java.io.BufferedReader.  Argument may be an instance of Reader
  BufferedReader, InputStream, File, URI, URL, Socket, or String.

  If argument is a String, it tries to resolve it first as a URI, then
  as a local file name.  URIs with a 'file' protocol are converted to
  local file names.  Uses *default-encoding* as the text encoding.

  Should be used inside with-open to ensure the Reader is properly
  closed."
             :arglists '([x])}
  reader class)

(defmethod reader Reader [x]
  (BufferedReader. x))

(defmethod reader InputStream [#^InputStream x]
  (BufferedReader. (InputStreamReader. x *default-encoding*)))

(defmethod reader File [#^File x]
  (reader (FileInputStream. x)))

(defmethod reader URL [#^URL x]
  (reader (if (= "file" (.getProtocol x))
            (FileInputStream. (.getPath x))
            (.openStream x))))

(defmethod reader URI [#^URI x]
  (reader (.toURL x)))

(defmethod reader String [#^String x]
  (try (let [url (URL. x)]
         (reader url))
       (catch MalformedURLException e
         (reader (File. x)))))

(defmethod reader Socket [#^Socket x]
  (reader (.getInputStream x)))

(defmethod reader :default [x]
  (throw (Exception. (str "Cannot open " (pr-str x) " as a reader."))))

(def
 #^{:doc "If true, writer and spit will open files in append mode.
 Defaults to false.  Use append-writer or append-spit."
    :tag Boolean}
 *append-to-writer* false)

(defmulti #^{:tag PrintWriter
             :doc "Attempts to coerce its argument into an open
  java.io.PrintWriter wrapped around a java.io.BufferedWriter.
  Argument may be an instance of Writer, PrintWriter, BufferedWriter
  OutputStream, File URI, URL, Socket, or String.

  If argument is a String, it tries to resolve it first as a URI, then
  as a local file name.  URIs with a 'file' protocol are converted to
  local file names.

  Should be used inside with-open to ensure the Writer is properly
  closed."
             :arglists '([x])}
  writer class)

(defn- assert-not-appending []
  (when *append-to-writer*
    (throw (Exception. "Cannot change an open stream to append mode."))))

(defmethod writer PrintWriter [x]
  (assert-not-appending)
  x)

(defmethod writer BufferedWriter [#^BufferedWriter x]
  (assert-not-appending)
  (PrintWriter. x))

(defmethod writer Writer [x]
  (assert-not-appending)
  ;; Writer includes sub-classes such as FileWriter
  (PrintWriter. (BufferedWriter. x)))

(defmethod writer OutputStream [#^OutputStream x]
  (assert-not-appending)
  (PrintWriter.
   (BufferedWriter.
    (OutputStreamWriter. x *default-encoding*))))

(defmethod writer File [#^File x]
  (let [stream (FileOutputStream. x *append-to-writer*)]
    (binding [*append-to-writer* false]
      (writer stream))))

(defmethod writer URL [#^URL x]
  (if (= "file" (.getProtocol x))
    (writer (File. (.getPath x)))
    (throw (Exception. (str "Cannot write to non-file URL <" x ">")))))

(defmethod writer URI [#^URI x]
  (writer (.toURL x)))

(defmethod writer String [#^String x]
  (try (let [url (URL. x)]
         (writer url))
       (catch MalformedURLException err
         (writer (File. x)))))

(defmethod writer Socket [#^Socket x]
  (writer (.getOutputStream x)))

(defmethod writer :default [x]
  (throw (Exception. (str "Cannot open <" (pr-str x) "> as a writer."))))

(defn append-writer
  "Like writer but opens file for appending.  Does not work on streams
  that are already open."
  [x]
  (binding [*append-to-writer* true]
    (writer x)))

(defn write-lines
  "Writes lines (a seq) to f, separated by newlines.  f is opened with
  writer, and automatically closed at the end of the sequence."
  [f lines]
  (with-open [#^PrintWriter writer (writer f)]
    (loop [lines lines]
      (when-let [line (first lines)]
        (.write writer (str line))
        (.println writer)
        (recur (rest lines))))))

(defn #^String slurp*
  "Like clojure.core/slurp but opens f with reader."
  [f]
  (with-open [#^BufferedReader r (reader f)]
    (let [sb (StringBuilder.)]
      (loop [c (.read r)]
        (if (neg? c)
          (str sb)
          (do (.append sb (char c))
              (recur (.read r))))))))

;; TODO: possibly move slurp here in a future version?

(defn spit
  "Opposite of slurp.  Opens f with writer, writes content, then
  closes f."
  [f content]
  (with-open [#^PrintWriter w (writer f)]
    (.print w content)))

(defn append-spit
  "Like spit but appends to file."
  [f content]
  (with-open [#^PrintWriter w (append-writer f)]
    (.print w content)))

(defmacro with-out-writer
  "Opens a writer on f, binds it to *out*, and evalutes body.
  Anything printed within body will be written to f."
  [f & body]
  `(with-open [stream# (writer ~f)]
     (binding [*out* stream#]
       ~@body)))

(defmacro with-out-append-writer
  "Like with-out-writer but appends to file."
  [f & body]
  `(with-open [stream# (append-writer ~f)]
     (binding [*out* stream#]
       ~@body)))

(defmacro with-in-reader
  "Opens a PushbackReader on f, binds it to *in*, and evaluates body."
  [f & body]
  `(with-open [stream# (PushbackReader. (reader ~f))]
     (binding [*in* stream#]
       ~@body)))

(defmulti
  #^{:doc "Copies input to output.  Returns nil.
  Input may be an InputStream, Reader, File, byte[], or String.
  Output may be an OutputStream, Writer, or File.

  Does not close any streams except those it opens itself
  (on a File).

  Writing a File fails if the parent directory does not exist."
     :arglists '([input output])}
  copy
  (fn [input output] [(type input) (type output)]))

(defmethod copy [InputStream OutputStream]
  [#^InputStream input #^OutputStream output]
  (let [buffer (make-array Byte/TYPE *buffer-size*)]
    (loop []
      (let [size (.read input buffer)]
        (when (pos? size)
          (do (.write output buffer 0 size)
              (recur)))))))

(defmethod copy [InputStream Writer] [#^InputStream input #^Writer output]
  (let [#^"[B" buffer (make-array Byte/TYPE *buffer-size*)]
    (loop []
      (let [size (.read input buffer)]
        (when (pos? size)
          (let [chars (.toCharArray (String. buffer 0 size *default-encoding*))]
            (do (.write output chars)
                (recur))))))))

(defmethod copy [InputStream File] [#^InputStream input #^File output]
  (with-open [out (FileOutputStream. output)]
    (copy input out)))

(defmethod copy [Reader OutputStream] [#^Reader input #^OutputStream output]
  (let [#^"[C" buffer (make-array Character/TYPE *buffer-size*)]
    (loop []
      (let [size (.read input buffer)]
        (when (pos? size)
          (let [bytes (.getBytes (String. buffer 0 size) *default-encoding*)]
            (do (.write output bytes)
                (recur))))))))

(defmethod copy [Reader Writer] [#^Reader input #^Writer output]
  (let [#^"[C" buffer (make-array Character/TYPE *buffer-size*)]
    (loop []
      (let [size (.read input buffer)]
        (when (pos? size)
          (do (.write output buffer 0 size)
              (recur)))))))

(defmethod copy [Reader File] [#^Reader input #^File output]
  (with-open [out (FileOutputStream. output)]
    (copy input out)))

(defmethod copy [File OutputStream] [#^File input #^OutputStream output]
  (with-open [in (FileInputStream. input)]
    (copy in output)))

(defmethod copy [File Writer] [#^File input #^Writer output]
  (with-open [in (FileInputStream. input)]
    (copy in output)))

(defmethod copy [File File] [#^File input #^File output]
  (with-open [in (FileInputStream. input)
              out (FileOutputStream. output)]
    (copy in out)))

(defmethod copy [String OutputStream] [#^String input #^OutputStream output]
  (copy (StringReader. input) output))

(defmethod copy [String Writer] [#^String input #^Writer output]
  (copy (StringReader. input) output))

(defmethod copy [String File] [#^String input #^File output]
  (copy (StringReader. input) output))

(defmethod copy [byte-array-type OutputStream]
  [#^"[B" input #^OutputStream output]
  (copy (ByteArrayInputStream. input) output))

(defmethod copy [byte-array-type Writer] [#^"[B" input #^Writer output]
  (copy (ByteArrayInputStream. input) output))

(defmethod copy [byte-array-type File] [#^"[B" input #^Writer output]
  (copy (ByteArrayInputStream. input) output))

(defn mkdir
  "Creates the directory described by path and its ancestors. path is
  interpreted the same as the arguments to the \"file\" function."
  [& path]
  (.mkdirs (apply file path)))

(defmulti
  #^{:doc "Converts argument into a Java byte array.  Argument may be
  a String, File, InputStream, or Reader.  If the argument is already
  a byte array, returns it."
    :arglists '([arg])}
  to-byte-array type)

(defmethod to-byte-array byte-array-type [x] x)

(defmethod to-byte-array String [#^String x]
  (.getBytes x *default-encoding*))

(defmethod to-byte-array File [#^File x]
  (with-open [input (FileInputStream. x)
              buffer (ByteArrayOutputStream.)]
    (copy input buffer)
    (.toByteArray buffer)))

(defmethod to-byte-array InputStream [#^InputStream x]
  (let [buffer (ByteArrayOutputStream.)]
    (copy x buffer)
    (.toByteArray buffer)))

(defmethod to-byte-array Reader [#^Reader x]
  (.getBytes (slurp* x) *default-encoding*))

(defmulti relative-path-string
  "Interpret a String or java.io.File as a relative path string.
   Building block for clojure.io/file."
  class)

(defmethod relative-path-string String [#^String s]
  (relative-path-string (File. s)))

(defmethod relative-path-string File [#^File f]
  (if (.isAbsolute f)
    (throw (IllegalArgumentException. (str f " is not a relative path")))
    (.getPath f)))

(defmulti #^File as-file
  "Interpret a String or a java.io.File as a File. Building block
   for clojure.io/file, which you should prefer in most cases."
  class)
(defmethod as-file String [#^String s] (File. s))
(defmethod as-file File [f] f)

(defn #^File file
  "Returns a File object from arguments. Each argument may be a string
  or File object and is treated as a segment of the final filename."
  ([arg]
     (as-file arg))
  ([parent child]
     (let [parent (if (.startsWith parent "~")
                    (str (System/getProperty "user.home")
                         File/separator (subs parent 1))
                    parent)]
       (File. #^File (as-file parent) #^String (relative-path-string child))))
  ([parent child & more]
     (reduce file (file parent child) more)))

(defn delete-file
  "Delete file f. Raise an exception if it fails unless silently is true."
  [f & [silently]]
  (or (.delete (file f))
      silently
      (throw (java.io.IOException. (str "Couldn't delete " f)))))

(defn delete-file-recursively
  "Delete file f. If it's a directory, recursively delete all its contents.
Raise an exception if any deletion fails unless silently is true."
  [f & [silently]]
  (let [f (file f)]
    (if (.isDirectory f)
      (doseq [child (.listFiles f)]
        (delete-file-recursively child silently)))
    (delete-file f silently)))
