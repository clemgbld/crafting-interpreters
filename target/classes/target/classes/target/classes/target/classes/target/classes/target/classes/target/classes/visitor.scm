(define (make-binary operator left right) 
    (list 'binary operator left right))

(define (make-unary operator  right) 
    (list 'unary operator right))

(define (make-literal expr) 
    (list 'literal expr))

(define (make-grouping expr) 
    (list 'grouping expr))

(define (ast-print expr)
  (define type (car expr))
  (cond 
    ( (= type 'binary) (string-append 
                         "(" 
                         (cadr expr) 
                         " "
                         (ast-print (caddr expr) )
                         " "
                         (ast-print (cadddr expr) )
                          ")"))
    ((= type 'unary) (string-append 
                         "(" 
                         (cadr expr) 
                         " "
                         (ast-print (caddr expr) )
                          ")"))
    ( (= type 'grouping) (string-append 
                         "(" 
                         "group" 
                         " "
                         (ast-print (cadr expr) )
                          ")") )
    ( (= type 'literal) (if (= (cadr expr) '()) "nil" (cadr expr)))
    (else (error "UNKNOWN TYPE" expr) )
    )
  )


