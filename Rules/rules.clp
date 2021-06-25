;;Define a rule for MacBook Air

(defrule macbook-offer
   (line-item (order-number ?on) (customer-id ?cid) (part-number ?pn))
   (product (part-number ?pn) (name ?n) (category ?pc) (price ?p)) (test(eq ?n "MacBook Air")) 
   (customer (customer-id ?cid)(name ?cn))
   =>
   (bind ?d (/ ?p 1000)) 
   (bind ?vale (* ?d 100))
   (printout t ?cn " eres acreedor de " ?vale " pesos en vales" crlf))

;;Define a rule for funda y mica

(defrule smartphone
   (line-item (order-number ?on) (part-number ?pn) (customer-id ?cid))
   (product (part-number ?pn) (name ?n) (category ?pc)) (test(eq ?pc Smartphone))
   (customer (customer-id ?cid)(name ?cn))
   =>
   (printout t ?cn " puedes comprar una funda y una mica con 15% de descuento" crlf))

;; Define a rule for iPhone ...

(defrule iphone-offer 
	(line-item (order-number ?on) (customer-id ?cid) (part-number ?pn))
        (product (part-number ?pn) (name ?n)) (test(eq ?n "iPhone 12 Pro Max"))
	(customer (customer-id ?cid)(name ?cn))
=>
(printout t ?cn " puedes realizar esta compra a 24 meses sin intereses" crlf))


;; Define a rule for Samsung ...
(defrule samsung-offer 
         (line-item (order-number ?on) (customer-id ?cid) (part-number ?pn))
         (product (part-number ?pn) (name ?n)) (test(eq ?n "Samsung Note 12"))
	 (customer (customer-id ?cid)(name ?cn) (credit-card ?cc)) (test(eq ?cc "Liverpool VISA"))
=>
(printout t ?cn " puedes realizar esta compra a 12 meses sin intereses" crlf))