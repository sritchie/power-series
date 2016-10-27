;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; SERIES ACCELERATION
;; improves rate of convergence for reducing functions

(defn euler-transform [s]
  (let [s0 (nth s 0)           
        s1 (nth s 1)           
        s2 (nth s 2)]
    (lazy-seq
     (cons (with-precision 1000
             (- s2 (/ (* (- s2 s1) (- s2 s1))
                      (+ s0 (* -2 s1) s2))))
           (euler-transform (rest s))))))

(defn make-tableau [transform s]
  (lazy-seq
   (cons s
         (make-tableau transform
                       (transform s)))))

(defn accelerate-sequence [transform s]
  (map first
       (make-tableau transform s)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(defn pi-accelerated [precision]
  (with-precision 1000
    (* 4M
       (reduce +
               (take precision
                     (accelerate-sequence euler-transform
                                          (arctan-series)))))))

(defn ln2 [precision]
  (defn ln2-loop [n]
    (lazy-seq
     (cons (with-precision 100 (/ 1M n))
           (map -
                (ln2-loop (+ n 1))))))
  (reduce +
          (take precision
                (ln2-loop 1))))

(defn ln2-accelerated [precision]
  (defn ln2-loop [n]
    (lazy-seq
     (cons (with-precision 100 (/ 1M n))
           (map -
                (ln2-loop (+ n 1))))))
  (reduce +
          (take precision
                (ln2-loop euler-transform
                          (ln2-loop 1)))))
