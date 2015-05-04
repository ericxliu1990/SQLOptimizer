aggMe.ccccccc_o_orderdate = o_orderdate;
aggMe.ccccccc_o_orderstatus = o_orderstatus;
aggMe.att1 = Str("status: ") + o_orderstatus;
aggMe.att2 = Str("date: ") + o_orderdate;
aggMe.att3 = aggMe.att3 + o_totalprice * Int (100);
aggMe.att4 = aggMe.att4 + Int (1);
aggMe.ccccccc_count = aggMe.ccccccc_count + Int (1);
